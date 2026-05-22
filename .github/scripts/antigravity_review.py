import asyncio
import os
from google.antigravity import Agent, LocalAgentConfig

async def main():
    diff_path = "pr_diff.txt"

    if not os.path.exists(diff_path) or os.path.getsize(diff_path) == 0:
        with open("review_result.md", "w", encoding="utf-8") as f:
            f.write("변경된 코드 내역을 찾을 수 없거나 Git diff 생성에 실패했습니다.")
        return

    with open(diff_path, "r", encoding="utf-8") as f:
        git_diff = f.read()

    config = LocalAgentConfig()
    review_prompt = f"""당신은 모든 프로그래밍 언어와 아키텍처에 능통한 '시니어 소프트웨어 엔지니어'입니다.
아래 제공된 Git Diff(변경 사항)를 분석하여 깊이 있는 코드 리뷰를 진행해 주세요.
해당 코드가 어떤 언어(Java, Python, JS, Go 등)인지 스스로 파악하고 그 언어의 최신 모범 사례(Best Practice)를 기준으로 평가하세요.

다음 3가지 영역을 중점적으로 검토해 주세요:
1. 치명적인 버그 및 보안 취약점 (로직 오류, 인젝션, 메모리 누수 등)
2. 성능 병목 (비효율적인 알고리즘, N+1 쿼리 등 불필요한 DB/네트워크 호출)
3. 코드 품질 및 유지보수성 (가독성 향상을 위한 리팩토링 제안, 네이밍 일관성 등)

단순한 오타 지적보다는 아키텍처나 성능 관점의 유의미한 피드백을 우선시해 주세요.
결과는 한국어로, 깔끔한 마크다운 형식으로 작성해 주세요.

Git Diff:
```diff
{git_diff.replace(chr(96)*3, chr(96) + ' ' + chr(96) + ' ' + chr(96))}
```"""

    try:
        async with Agent(config) as agent:
            response = await agent.chat(review_prompt)
            review_text = await response.text()
    except Exception as e:
        review_text = f"AI 리뷰 생성 중 오류가 발생했습니다: {e}"

    with open("review_result.md", "w", encoding="utf-8") as f:
        f.write("## Antigravity AI Code Review\n\n")
        f.write(review_text)

if __name__ == "__main__":
    asyncio.run(main())
