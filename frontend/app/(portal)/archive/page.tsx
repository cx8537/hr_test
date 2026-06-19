"use client";

// 문서 아카이브 화면(DOC-004/007). 제목·파일명 검색, 결과 목록(공개범위 뱃지), 백엔드 경유 다운로드.
import { useState } from "react";
import { RequireAuth } from "@/components/require-auth";
import {
  searchDocuments,
  documentDownloadUrl,
  type ArchiveDocumentResponse,
} from "@/lib/api";
import { visibilityLabel, sourceLabel } from "@/lib/document-view";

export default function ArchivePage() {
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<ArchiveDocumentResponse[]>([]);
  const [error, setError] = useState<string | null>(null);

  const onSearch = async () => {
    setError(null);
    try {
      setResults(await searchDocuments(keyword));
    } catch {
      setError("검색에 실패했습니다.");
    }
  };

  return (
    <RequireAuth>
      <div className="mx-auto max-w-3xl space-y-4 p-4">
        <h1 className="text-xl font-semibold">문서 아카이브</h1>
        <div className="flex gap-2">
          <input
            aria-label="검색어"
            className="flex-1 border px-2 py-1 text-sm"
            placeholder="제목 또는 파일명"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
          />
          <button
            type="button"
            className="rounded border px-3 py-1 text-sm"
            onClick={onSearch}
          >
            검색
          </button>
        </div>
        {error && <p className="text-sm text-red-600">{error}</p>}

        <table className="w-full text-sm">
          <thead>
            <tr className="text-left text-gray-600">
              <th className="py-1">제목</th>
              <th>출처</th>
              <th>공개범위</th>
              <th>다운로드</th>
            </tr>
          </thead>
          <tbody>
            {results.map((d) => (
              <tr key={d.id}>
                <td className="py-1">{d.title}</td>
                <td>{sourceLabel(d.source)}</td>
                <td>{visibilityLabel(d.visibility)}</td>
                <td>
                  <a className="text-blue-600 underline" href={documentDownloadUrl(d.id)}>
                    내려받기
                  </a>
                </td>
              </tr>
            ))}
            {results.length === 0 && (
              <tr>
                <td colSpan={4} className="py-2 text-gray-500">
                  검색 결과가 없습니다.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </RequireAuth>
  );
}
