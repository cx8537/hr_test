import { describe, expect, it } from "vitest";
import { visibilityLabel, sourceLabel } from "@/lib/document-view";

// DOC-005: 공개범위·출처 라벨.
describe("document-view (DOC-005)", () => {
  it("공개범위 라벨", () => {
    expect(visibilityLabel("PUBLIC")).toBe("전사공개");
    expect(visibilityLabel("INVOLVED_ONLY")).toBe("관여자한정");
  });

  it("출처 라벨", () => {
    expect(sourceLabel("APPROVAL")).toBe("결재문서");
    expect(sourceLabel("UPLOAD")).toBe("업로드");
  });
});
