// 문서 아카이브 화면 보조 순수 함수(DOC-005). 공개범위·출처 라벨.
import type { DocumentSource, DocumentVisibility } from "./api";

export function visibilityLabel(v: DocumentVisibility): string {
  return v === "PUBLIC" ? "전사공개" : "관여자한정";
}

export function sourceLabel(s: DocumentSource): string {
  return s === "APPROVAL" ? "결재문서" : "업로드";
}
