import json
import os
import urllib.request
import urllib.error
from google import genai
from google.genai import types
from pydantic import BaseModel, Field

# 1. AI 응답을 규격화하기 위한 Pydantic 모델 정의
class ReviewComment(BaseModel):
    path: str = Field(description="The relative file path (e.g. 'src/main/java/App.java') as shown in the git diff.")
    line: int = Field(description="The 1-indexed line number in the new file where the comment should be placed. MUST be a line that was added or modified in the diff.")
    body: str = Field(description="The code review comment in Korean. Use markdown formatting. Provide constructive feedback, point out bugs, or suggest optimizations.")

class PullRequestReview(BaseModel):
    summary: str = Field(description="An overall summary of the PR review in Korean. Use markdown formatting.")
    comments: list[ReviewComment] = Field(description="List of inline comments to place on specific changed lines.")

# 2. Git Diff 파싱: 실제 변경된 파일과 라인 번호를 추출하는 함수
def parse_changed_lines(diff_path: str) -> dict[str, set[int]]:
    if not os.path.exists(diff_path) or os.path.getsize(diff_path) == 0:
        return {}

    changed_lines = {}
    current_file = None
    current_line = 0

    with open(diff_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    for line in lines:
        if line.startswith('diff --git'):
            current_file = None
        elif line.startswith('+++ b/'):
            # 파일 경로 추출 (예: '+++ b/src/main/java/App.java' -> 'src/main/java/App.java')
            current_file = line[6:].strip()
            changed_lines[current_file] = set()
        elif line.startswith('@@'):
            # 예: @@ -10,7 +12,8 @@ -> 신규 파일 시작 라인은 12
            parts = line.split(' ')
            if len(parts) >= 3:
                new_info = parts[2]
                if ',' in new_info:
                    current_line = int(new_info.split(',')[0].replace('+', ''))
                else:
                    current_line = int(new_info.replace('+', ''))
        elif current_file is not None:
            if line.startswith('+') and not line.startswith('+++'):
                changed_lines[current_file].add(current_line)
                current_line += 1
            elif line.startswith('-') and not line.startswith('---'):
                # 이전 파일에서 삭제된 라인이므로 신규 파일 라인 번호는 올라가지 않음
                pass
            else:
                # 변경되지 않고 컨텍스트로 제공된 라인
                current_line += 1

    return changed_lines

def main():
    diff_path = "pr_diff.txt"
    changed_lines_map = parse_changed_lines(diff_path)

    if not changed_lines_map:
        print("변경된 코드 내역이 없거나 Git diff 생성에 실패했습니다.")
        return

    # Git diff 전체 텍스트 읽기
    with open(diff_path, "r", encoding="utf-8") as f:
        git_diff = f.read()

    # 3. Gemini API 호출
    client = genai.Client()
    
    review_prompt = f"""당신은 모든 프로그래밍 언어와 아키텍처에 능통한 '시니어 소프트웨어 엔지니어'이자 코드 리뷰어입니다.
아래 제공된 Git Diff(변경 사항)를 분석하여 코드 리뷰를 진행해 주세요.

다음 3가지 영역을 중점적으로 검토해 주세요:
1. 치명적인 버그 및 보안 취약점 (로직 오류, 인젝션, 메모리 누수 등)
2. 성능 병목 (비효율적인 알고리즘, N+1 쿼리 등 불필요한 DB/네트워크 호출)
3. 코드 품질 및 유지보수성 (가독성 향상을 위한 리팩토링 제안, 네이밍 일관성 등)

**주의사항**:
- 전체적인 요약은 `summary` 필드에 작성해 주세요.
- 코드의 특정 위치에 상세하게 제안할 사항이 있는 경우에만 `comments` 목록에 개별 코멘트를 추가해 주세요.
- **반드시** 제공된 Git Diff 상에서 추가되거나 수정된 라인(라인 번호가 존재하고 '+'로 시작하는 줄)에 대해서만 코멘트를 작성해야 합니다. 변경되지 않은 라인 번호에 코멘트를 작성하면 오류가 발생합니다.

Git Diff:
```diff
{git_diff}
```"""

    try:
        response = client.models.generate_content(
            model='gemini-2.5-flash',
            contents=review_prompt,
            config=types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=PullRequestReview,
                temperature=0.2,
            ),
        )
        # 응답 파싱
        review_data = json.loads(response.text)
    except Exception as e:
        print(f"Gemini API 호출 또는 JSON 파싱 중 오류 발생: {e}")
        if 'response' in locals() and response.text:
            print("원본 응답 내용:", response.text)
        return

    # 4. 유효하지 않은 라인(diff에 포함되지 않은 라인)에 달린 댓글 필터링
    valid_comments = []
    for comment in review_data.get("comments", []):
        path = comment.get("path")
        line = comment.get("line")
        body = comment.get("body")
        
        if not path or not line or not body:
            continue
            
        # path 경로 포맷 클렌징 (슬래시 변환)
        path = path.replace("\\", "/")
        
        # 파일이 변경 목록에 있고, 해당 라인이 실제로 변경되었는지 유효성 검사
        if path in changed_lines_map and line in changed_lines_map[path]:
            valid_comments.append({
                "path": path,
                "line": line,
                "body": body
            })
        else:
            print(f"유효하지 않은 댓글 위치 필터링됨 - 파일: {path}, 라인: {line}")

    # 5. GitHub API로 PR 리뷰 게시
    token = os.getenv("GITHUB_TOKEN")
    repo = os.getenv("GITHUB_REPOSITORY")
    pr_number = os.getenv("GITHUB_PR_NUMBER")
    event_path = os.getenv("GITHUB_EVENT_PATH")

    if not all([token, repo, pr_number, event_path]):
        print("GitHub 환경변수가 부족하여 API를 호출할 수 없습니다. 로컬 테스트 모드로 종료합니다.")
        # 디버깅용으로 결과 파일 저장
        with open("review_result_local.json", "w", encoding="utf-8") as f:
            json.dump({
                "summary": review_data.get("summary"),
                "comments": valid_comments
            }, f, indent=2, ensure_ascii=False)
        return

    # 이벤트 페이로드 파일에서 최신 Commit SHA 파싱
    try:
        with open(event_path, "r", encoding="utf-8") as f:
            event_data = json.load(f)
        commit_sha = event_data["pull_request"]["head"]["sha"]
    except Exception as e:
        print(f"이벤트 페이로드에서 Commit SHA 추출 실패: {e}")
        return

    # API Payload 생성
    # comments에 side="RIGHT"를 지정해야 추가/변경된 우측 라인에 댓글이 정확히 달립니다.
    github_comments = []
    for vc in valid_comments:
        github_comments.append({
            "path": vc["path"],
            "line": vc["line"],
            "side": "RIGHT",
            "body": vc["body"]
        })

    payload = {
        "commit_id": commit_sha,
        "body": review_data.get("summary", "AI가 코드 리뷰를 완료했습니다."),
        "event": "COMMENT", # 승인 없이 단순 피드백 형태로 등록
        "comments": github_comments
    }

    url = f"https://api.github.com/repos/{repo}/pulls/{pr_number}/reviews"
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "X-GitHub-Api-Version": "2022-11-28",
        "Content-Type": "application/json"
    }

    req = urllib.request.Request(
        url,
        data=json.dumps(payload).encode("utf-8"),
        headers=headers,
        method="POST"
    )

    try:
        with urllib.request.urlopen(req) as response:
            res_body = response.read().decode("utf-8")
            print("GitHub PR 리뷰가 성공적으로 등록되었습니다.")
    except urllib.error.HTTPError as e:
        print(f"GitHub API 호출 중 에러 발생 (상태 코드: {e.code})")
        print("에러 내용:", e.read().decode("utf-8"))
    except Exception as e:
        print(f"네트워크 요청 실패: {e}")

if __name__ == "__main__":
    main()
