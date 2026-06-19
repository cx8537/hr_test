// 결재 양식 클라이언트 측 계산·검증 순수 함수(AP-041/042/045).
// 화면에서 금액·일수를 즉시 표시하기 위한 보조 계산이며, 최종 권위는 백엔드에 있다.

export type FormType = "EXPENSE" | "LEAVE" | "ASSET_REQ" | "GENERAL";

export interface AmountLine {
  quantity: number;
  unitPrice: number;
}

/** 행 금액 = 수량 × 단가(AP-041 AC1). 음수 거부. */
export function expenseAmount(quantity: number, unitPrice: number): number {
  if (quantity < 0) throw new Error("수량은 음수일 수 없습니다.");
  if (unitPrice < 0) throw new Error("단가는 음수일 수 없습니다.");
  return quantity * unitPrice;
}

/** 합계 = Σ 행 금액(AP-041 AC2). */
export function expenseTotal(lines: AmountLine[]): number {
  return lines.reduce((sum, l) => sum + expenseAmount(l.quantity, l.unitPrice), 0);
}

function parseDate(iso: string): Date {
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d);
}

function toIso(d: Date): string {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

function isBusinessDay(d: Date, holidays: Set<string>): boolean {
  const dow = d.getDay(); // 0=일, 6=토
  if (dow === 0 || dow === 6) return false;
  return !holidays.has(toIso(d));
}

/**
 * 휴가 일수 계산(AP-042). 영업일 기준(토·일·공휴일 제외 AC1/AC3), 반차=0.5일·단일 날짜만(AC2),
 * 종료<시작 거부(AC4). 백엔드 LeaveDaysCalculator와 동일 규칙(표시용).
 */
export function leaveDays(
  start: string,
  end: string,
  halfDay: boolean,
  holidays: string[] = [],
): number {
  const s = parseDate(start);
  const e = parseDate(end);
  if (e < s) throw new Error("종료일이 시작일보다 빠를 수 없습니다.");
  const holiSet = new Set(holidays);
  if (halfDay) {
    if (start !== end) throw new Error("반차는 단일 날짜에만 허용됩니다.");
    return isBusinessDay(s, holiSet) ? 0.5 : 0;
  }
  let count = 0;
  for (const d = new Date(s); d <= e; d.setDate(d.getDate() + 1)) {
    if (isBusinessDay(d, holiSet)) count++;
  }
  return count;
}

/** 일반 품의서 본문 필수(AP-045 AC1). */
export function isGeneralBodyValid(body: string): boolean {
  return body.trim().length > 0;
}

/**
 * 승인 서명 payload(AP-034). 백엔드 ApprovalService.signaturePayload와 동일 포맷:
 * `doc:{documentId}:r{round}:{approverId}`.
 */
export function signaturePayload(
  documentId: number,
  round: number,
  approverId: number,
): string {
  return `doc:${documentId}:r${round}:${approverId}`;
}
