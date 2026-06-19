import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";

// 골격 단계 스모크 테스트: 테스트 러너(Vitest)와 Testing Library가 동작하는지 확인.
function Hello() {
  return <h1>HR_Test_05</h1>;
}

describe("smoke", () => {
  it("renders a component", () => {
    render(<Hello />);
    expect(screen.getByText("HR_Test_05")).toBeInTheDocument();
  });
});
