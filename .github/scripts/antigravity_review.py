import json
import os
import re
import sys
import urllib.request
import urllib.error
from google import genai
from pydantic import BaseModel


class ReviewComment(BaseModel):
    path: str
    line: int
    body: str


class PullRequestReview(BaseModel):
    summary: str
    comments: list[ReviewComment]


def parse_changed_lines(diff: str) -> dict[str, set[int]]:
    """diff에서 파일별 변경된 라인 번호(오른쪽 기준) 추출"""
    changed = {}
    current_file = None
    current_line = 0

    for line in diff.splitlines():
        if line.startswith("+++ b/"):
            current_file = line[6:]
            changed.setdefault(current_file, set())
        elif line.startswith("@@ "):
            match = re.search(r"\+(\d+)", line)
            if match:
                current_line = int(match.group(1)) - 1
        elif current_file:
            if line.startswith("+") and not line.startswith("+++"):
                current_line += 1
                changed[current_file].add(current_line)
            elif not line.startswith("-"):
                current_line += 1

    return changed


def post_github_review(repo: str, pr_number: str, head_sha: str,
                       token: str, summary: str, comments: list[dict]) -> None:
    payload = {
        "commit_id": head_sha,
        "event": "COMMENT",
        "body": summary,
        "comments": comments,
    }
    url = f"https://api.github.com/repos/{repo}/pulls/{pr_number}/reviews"
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=data,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "X-GitHub-Api-Version": "2022-11-28",
        },
        method="POST",
    )
    try:
        with urllib.request.urlopen(req) as resp:
            print(f"GitHub Review 등록 완료: {resp.status}")
    except urllib.error.HTTPError as e:
        print(f"GitHub API 오류: {e.code} {e.read().decode()}")
        sys.exit(1)


def main():
    diff_path = os.environ.get("PR_DIFF_PATH", "pr_diff.txt")
    pr_number = os.environ.get("PR_NUMBER")
    repo = os.environ.get("GITHUB_REPOSITORY")
    head_sha = os.environ.get("HEAD_SHA")
    token = os.environ.get("GITHUB_TOKEN")
    api_key = os.environ.get("GEMINI_API_KEY")

    if not os.path.exists(diff_path) or os.path.getsize(diff_path) == 0:
        print("변경된 코드 내역이 없습니다.")
        sys.exit(0)

    with open(diff_path, "r", encoding="utf-8") as f:
        git_diff = f.read()

    changed_lines = parse_changed_lines(git_diff)
    safe_diff = git_diff.replace("```", "'' '")

    review_prompt = f"""당신은 모든 프로그래밍 언어와 아키텍처에 능통한 '시니어 소프트웨어 엔지니어'입니다.
아래 제공된 Git Diff를 분석하여 깊이 있는 코드 리뷰를 진행해 주세요.
해당 코드가 어떤 언어인지 스스로 파악하고 그 언어의 최신 모범 사례(Best Practice)를 기준으로 평가하세요.

다음 3가지 영역을 중점적으로 검토해 주세요:
1. 치명적인 버그 및 보안 취약점 (로직 오류, 인젝션, 메모리 누수 등)
2. 성능 병목 (비효율적인 알고리즘, N+1 쿼리 등 불필요한 DB/네트워크 호출)
3. 코드 품질 및 유지보수성 (가독성 향상을 위한 리팩토링 제안, 네이밍 일관성 등)

단순한 오타 지적보다는 아키텍처나 성능 관점의 유의미한 피드백을 우선시해 주세요.
결과는 한국어로 작성해 주세요.

- summary: 전체 리뷰 총평 (마크다운 형식)
- comments: 인라인 댓글 목록. path는 저장소 루트 기준 파일 경로, line은 변경된 라인 번호, body는 해당 라인에 대한 구체적인 리뷰 내용

[Git Diff Source]
{safe_diff}
"""

    client = genai.Client(api_key=api_key)
    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=review_prompt,
            config=genai.types.GenerateContentConfig(
                response_mime_type="application/json",
                response_schema=PullRequestReview,
            ),
        )
        review: PullRequestReview = response.parsed
    except Exception as e:
        print(f"AI 리뷰 생성 중 오류가 발생했습니다: {e}")
        sys.exit(1)

    # 실제 변경된 라인에 해당하는 댓글만 필터링
    valid_comments = [
        {"path": c.path, "line": c.line, "side": "RIGHT", "body": c.body}
        for c in review.comments
        if c.path in changed_lines and c.line in changed_lines[c.path]
    ]

    post_github_review(repo, pr_number, head_sha, token, review.summary, valid_comments)


if __name__ == "__main__":
    main()
