// 백엔드 REST API fetch 클라이언트 (CSR 중심).
// 백엔드 베이스 URL은 환경변수로 외부화한다(포트 다름 → 개발 CORS 필요).
import { clearTokens, getAccessToken, setTokens } from "./auth";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

// 공통 fetch 래퍼. JSON 요청/응답을 기본으로 하고, 토큰이 있으면 Authorization 헤더를 첨부한다.
export async function apiFetch<T>(
  path: string,
  options: RequestInit = {},
): Promise<T> {
  const token = getAccessToken();
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!res.ok) {
    throw new ApiError(res.status, `API 요청 실패: ${res.status}`);
  }

  // 204 No Content 등 본문 없는 응답 처리
  if (res.status === 204) {
    return undefined as T;
  }
  return (await res.json()) as T;
}

// --- 인증 API (FND-003/004) ---
export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  mustChangePassword: boolean;
}

export async function login(loginId: string, password: string): Promise<LoginResponse> {
  const res = await apiFetch<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ loginId, password }),
  });
  setTokens(res.accessToken, res.refreshToken);
  return res;
}

export async function logout(): Promise<void> {
  try {
    await apiFetch<void>("/api/auth/logout", { method: "POST" });
  } finally {
    clearTokens();
  }
}

// --- 결재 API (AP-001/010/031/030/034/040) ---
import type { FormType } from "./approval-form";

export interface LineMemberInput {
  stepNo: number;
  approverId: number;
  stepType: "SEQUENTIAL" | "PARALLEL" | "CONSENT";
}

export interface ApprovalDocumentResponse {
  id: number;
  formType: FormType;
  title: string;
  status: string;
  currentRound: number;
}

/** 상신(AP-001/002). 결재선 스냅샷은 백엔드에서 고정된다. */
export async function submitDocument(payload: {
  formType: FormType;
  title: string;
  draftDeptId: number;
  line: LineMemberInput[];
}): Promise<ApprovalDocumentResponse> {
  return apiFetch<ApprovalDocumentResponse>("/api/approval/documents", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/** 승인(AP-010/034): 본인 개인키로 서명한 값을 공개키 ID와 함께 전송. */
export async function approveDocument(
  documentId: number,
  publicKeyId: number,
  signatureBase64: string,
): Promise<{ status: string }> {
  return apiFetch(`/api/approval/documents/${documentId}/approve`, {
    method: "POST",
    body: JSON.stringify({ publicKeyId, signatureBase64 }),
  });
}

/** 반려(AP-031): 사유 필수. */
export async function rejectDocument(
  documentId: number,
  reason: string,
): Promise<{ status: string }> {
  return apiFetch(`/api/approval/documents/${documentId}/reject`, {
    method: "POST",
    body: JSON.stringify({ reason }),
  });
}

/** 회수(AP-030): 상신자만, 무승인일 때만. */
export async function withdrawDocument(
  documentId: number,
): Promise<{ status: string }> {
  return apiFetch(`/api/approval/documents/${documentId}/withdraw`, {
    method: "POST",
  });
}

export interface AttachmentResponse {
  id: number;
  documentId: number;
  fileName: string;
  contentType: string;
  sizeBytes: number;
  uploaderId: number;
}

/** 첨부 업로드(AP-040): multipart. 백엔드가 MinIO 저장 후 메타 반환. */
export async function uploadAttachment(
  documentId: number,
  file: File,
): Promise<AttachmentResponse> {
  const token = getAccessToken();
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(
    `${API_BASE_URL}/api/approval/documents/${documentId}/attachments`,
    {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: form,
    },
  );
  if (!res.ok) throw new ApiError(res.status, `첨부 업로드 실패: ${res.status}`);
  return (await res.json()) as AttachmentResponse;
}

/** 첨부 다운로드 URL(AP-040): 백엔드 경유. presigned URL 미사용. */
export function attachmentDownloadUrl(attachmentId: number): string {
  return `${API_BASE_URL}/api/approval/attachments/${attachmentId}`;
}

// --- 거점 API (LOC-001~006) ---
export interface LocationResponse {
  id: number;
  locationCode: string;
  name: string;
  address: string | null;
  latitude: number | null;
  longitude: number | null;
  contact: string | null;
  fax: string | null;
  locationType: string | null;
  managerId: number | null;
  status: "ACTIVE" | "INACTIVE";
}

/** 활성 거점 목록(LOC-005). 지도/목록 뷰 공통 데이터셋. */
export async function listLocations(): Promise<LocationResponse[]> {
  return apiFetch<LocationResponse[]>("/api/locations");
}

/** 거점 등록(LOC-001, 시스템관리자). */
export async function createLocation(payload: {
  locationCode: string;
  name: string;
  address?: string;
  locationType?: string;
  managerId?: number;
}): Promise<LocationResponse> {
  return apiFetch<LocationResponse>("/api/locations", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/** 좌표 수정(LOC-002, 지도 핀/수동 입력). */
export async function updateLocationCoordinates(
  id: number,
  latitude: number,
  longitude: number,
): Promise<LocationResponse> {
  return apiFetch<LocationResponse>(`/api/locations/${id}/coordinates`, {
    method: "PUT",
    body: JSON.stringify({ latitude, longitude }),
  });
}

/** 담당자 지정(LOC-003). */
export async function assignLocationManager(
  id: number,
  managerId: number,
): Promise<LocationResponse> {
  return apiFetch<LocationResponse>(`/api/locations/${id}/manager`, {
    method: "PUT",
    body: JSON.stringify({ managerId }),
  });
}

export interface LocationPhotoResponse {
  id: number;
  locationId: number;
  fileName: string;
  contentType: string;
  sizeBytes: number;
}

/** 거점 사진 업로드(LOC-004): multipart, 백엔드 경유. */
export async function uploadLocationPhoto(
  locationId: number,
  file: File,
): Promise<LocationPhotoResponse> {
  const token = getAccessToken();
  const form = new FormData();
  form.append("file", file);
  const res = await fetch(`${API_BASE_URL}/api/locations/${locationId}/photos`, {
    method: "POST",
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: form,
  });
  if (!res.ok) throw new ApiError(res.status, `사진 업로드 실패: ${res.status}`);
  return (await res.json()) as LocationPhotoResponse;
}

/** 거점 사진 목록(LOC-004). */
export async function listLocationPhotos(
  locationId: number,
): Promise<LocationPhotoResponse[]> {
  return apiFetch<LocationPhotoResponse[]>(`/api/locations/${locationId}/photos`);
}

// --- 비품 API (AST-001~004) ---
export type AssetManagementType = "INDIVIDUAL" | "QUANTITY";
export type IndividualAssetStatus =
  | "USING"
  | "STORAGE"
  | "REPAIR"
  | "DISCARDED";
export type StockTransactionType = "IN" | "OUT";

export interface AssetItemResponse {
  id: number;
  locationId: number;
  name: string;
  managementType: AssetManagementType;
}

export interface IndividualAssetResponse {
  id: number;
  assetItemId: number;
  assetNumber: string;
  status: IndividualAssetStatus;
  acquisitionDate: string | null;
}

/** 품목 등록(AST-001, 비품관리자). */
export async function createAssetItem(payload: {
  locationId: number;
  name: string;
  managementType: AssetManagementType;
}): Promise<AssetItemResponse> {
  return apiFetch<AssetItemResponse>("/api/assets/items", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

/** 개체 등록(AST-002). */
export async function createIndividualAsset(
  itemId: number,
  payload: {
    assetNumber: string;
    status: IndividualAssetStatus;
    acquisitionDate?: string;
  },
): Promise<IndividualAssetResponse> {
  return apiFetch<IndividualAssetResponse>(
    `/api/assets/items/${itemId}/individuals`,
    { method: "POST", body: JSON.stringify(payload) },
  );
}

/** 개체 상태 변경(AST-002 AC2). */
export async function changeIndividualStatus(
  individualId: number,
  status: IndividualAssetStatus,
): Promise<IndividualAssetResponse> {
  return apiFetch<IndividualAssetResponse>(
    `/api/assets/individuals/${individualId}/status`,
    { method: "PUT", body: JSON.stringify({ status }) },
  );
}

/** 개체 폐기(AST-004: 보존). */
export async function discardIndividualAsset(
  individualId: number,
): Promise<IndividualAssetResponse> {
  return apiFetch<IndividualAssetResponse>(
    `/api/assets/individuals/${individualId}/discard`,
    { method: "POST" },
  );
}

/** 입출고 등록(AST-003 AC1). */
export async function recordStock(
  itemId: number,
  type: StockTransactionType,
  quantity: number,
): Promise<void> {
  return apiFetch<void>(`/api/assets/items/${itemId}/stock`, {
    method: "POST",
    body: JSON.stringify({ type, quantity }),
  });
}

/** 현재 수량 조회(AST-003 AC2, 파생). */
export async function getAssetQuantity(
  itemId: number,
): Promise<{ assetItemId: number; currentQuantity: number }> {
  return apiFetch(`/api/assets/items/${itemId}/quantity`);
}

export { API_BASE_URL };
