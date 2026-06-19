import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";
import { LoginForm } from "@/components/login-form";

describe("LoginForm (FND-003)", () => {
  it("입력값으로 onSubmit 호출", async () => {
    const onSubmit = vi.fn().mockResolvedValue(undefined);
    render(<LoginForm onSubmit={onSubmit} />);

    fireEvent.change(screen.getByLabelText("아이디"), { target: { value: "hong" } });
    fireEvent.change(screen.getByLabelText("비밀번호"), { target: { value: "pw123456" } });
    fireEvent.click(screen.getByRole("button", { name: "로그인" }));

    await waitFor(() => expect(onSubmit).toHaveBeenCalledWith("hong", "pw123456"));
  });

  it("로그인 실패 시 에러 메시지 노출", async () => {
    const onSubmit = vi.fn().mockRejectedValue(new Error("invalid"));
    render(<LoginForm onSubmit={onSubmit} />);

    fireEvent.click(screen.getByRole("button", { name: "로그인" }));

    expect(await screen.findByRole("alert")).toBeInTheDocument();
  });
});
