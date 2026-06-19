import { beforeEach, describe, expect, it } from "vitest";
import {
  clearTokens,
  getAccessToken,
  isAuthenticated,
  setTokens,
} from "@/lib/auth";

describe("auth token store (FND-003/004)", () => {
  beforeEach(() => {
    clearTokens();
  });

  it("저장 후 조회된다", () => {
    setTokens("access-1", "refresh-1");
    expect(getAccessToken()).toBe("access-1");
    expect(isAuthenticated()).toBe(true);
  });

  it("clear 후 미인증 상태", () => {
    setTokens("access-1", "refresh-1");
    clearTokens();
    expect(getAccessToken()).toBeNull();
    expect(isAuthenticated()).toBe(false);
  });
});
