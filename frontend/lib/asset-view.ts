// 비품 화면 보조 순수 함수(AST-003/004). 입출고 미리보기·상태 표시용. 권위는 백엔드.
import type { IndividualAssetStatus, StockTransactionType } from "./api";

/** 입출고 적용 후 예상 수량. 출고로 음수가 되면 거부(AST-003 AC3, 백엔드와 동일 규칙). */
export function previewQuantity(
  current: number,
  type: StockTransactionType,
  quantity: number,
): number {
  if (quantity < 0) throw new Error("수량은 음수일 수 없습니다.");
  const next = type === "IN" ? current + quantity : current - quantity;
  if (next < 0) throw new Error("재고가 부족하여 출고할 수 없습니다.");
  return next;
}

const STATUS_LABELS: Record<IndividualAssetStatus, string> = {
  USING: "사용중",
  STORAGE: "보관",
  REPAIR: "수리",
  DISCARDED: "폐기",
};

export function statusLabel(status: IndividualAssetStatus): string {
  return STATUS_LABELS[status];
}

/** 폐기 여부(AST-004: 폐기돼도 목록에서 제외하지 않고 상태로만 표시). */
export function isDiscarded(status: IndividualAssetStatus): boolean {
  return status === "DISCARDED";
}
