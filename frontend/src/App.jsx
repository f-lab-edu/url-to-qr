import React, { useEffect, useMemo, useState } from "react";
import { ArrowDownToLine, ArrowUpRight, Link2, QrCode, X } from "lucide-react";

const QR_API_BASE_URL = (
  window.__RUNTIME_CONFIG__?.VITE_QR_API_BASE_URL ||
  import.meta.env.VITE_QR_API_BASE_URL ||
  "http://localhost:8080"
).replace(/\/$/, "");
const QR_API_URL = `${QR_API_BASE_URL}/create-qr`;

function normalizeUrl(value) {
  const trimmed = value.trim();
  if (!trimmed) return "";
  if (trimmed.startsWith("//")) return `https:${trimmed}`;
  return /^[a-z][a-z\d+\-.]*:\/\//i.test(trimmed)
    ? trimmed
    : `https://${trimmed}`;
}

function isValidUrl(value) {
  if (!value) return false;
  try {
    new URL(value);
    return true;
  } catch {
    return false;
  }
}

function getDownloadFileName(contentDisposition) {
  if (!contentDisposition) return "qrcode_qr-code.png";
  const utf8Match = contentDisposition.match(/filename\*=UTF-8''([^;]+)/i);
  if (utf8Match) return `qrcode_${decodeURIComponent(utf8Match[1])}`;
  const fileNameMatch = contentDisposition.match(/filename="?([^";]+)"?/i);
  return `qrcode_${fileNameMatch?.[1] || "qr-code.png"}`;
}

async function getServerErrorMessage(response) {
  const fallbackMessage =
    response.status >= 500
      ? "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
      : "QR 코드 생성 요청을 처리하지 못했습니다.";

  try {
    const contentType = response.headers.get("Content-Type") || "";
    if (contentType.includes("json")) {
      const errorResponse = await response.json();
      return errorResponse.message || errorResponse.detail || fallbackMessage;
    }

    const message = (await response.text()).trim();
    return message || fallbackMessage;
  } catch {
    return fallbackMessage;
  }
}

function App() {
  const [url, setUrl] = useState("");
  const [qrImageUrl, setQrImageUrl] = useState("");
  const [qrFileName, setQrFileName] = useState("qr-code.png");
  const [requestError, setRequestError] = useState("");
  const [isGenerating, setIsGenerating] = useState(false);

  const normalizedUrl = useMemo(() => normalizeUrl(url), [url]);
  const hasInput = Boolean(url.trim());
  const hasValidUrl = isValidUrl(normalizedUrl);
  const validationError =
    hasInput && !hasValidUrl ? "올바른 URL을 입력해 주세요." : "";
  const displayError = validationError || requestError;
  const canDownload = Boolean(qrImageUrl && !isGenerating);

  useEffect(
    () => () => {
      if (qrImageUrl) URL.revokeObjectURL(qrImageUrl);
    },
    [qrImageUrl],
  );

  function handleUrlChange(event) {
    setUrl(event.target.value);
    setRequestError("");
    setQrImageUrl((previousUrl) => {
      if (previousUrl) URL.revokeObjectURL(previousUrl);
      return "";
    });
    setQrFileName("qr-code.png");
  }

  async function handleGenerate(event) {
    event.preventDefault();
    const requestUrl = normalizeUrl(url);
    if (!isValidUrl(requestUrl) || isGenerating) return;

    setUrl(requestUrl);
    setIsGenerating(true);
    setRequestError("");

    try {
      const response = await fetch(QR_API_URL, {
        method: "POST",
        headers: { "Content-Type": "text/plain;charset=UTF-8" },
        body: requestUrl,
      });

      if (!response.ok) {
        throw new Error(await getServerErrorMessage(response));
      }

      setQrFileName(
        getDownloadFileName(response.headers.get("Content-Disposition")),
      );
      const objectUrl = URL.createObjectURL(await response.blob());
      setQrImageUrl((previousUrl) => {
        if (previousUrl) URL.revokeObjectURL(previousUrl);
        return objectUrl;
      });
    } catch (error) {
      setQrImageUrl((previousUrl) => {
        if (previousUrl) URL.revokeObjectURL(previousUrl);
        return "";
      });
      setQrFileName("qr-code.png");
      setRequestError(
        error instanceof TypeError
          ? "서버에 연결할 수 없습니다. 잠시 후 다시 시도해 주세요."
          : error.message || "QR 코드 생성 중 오류가 발생했습니다.",
      );
    } finally {
      setIsGenerating(false);
    }
  }

  function handleDownload() {
    if (!canDownload) return;
    const anchor = document.createElement("a");
    anchor.href = qrImageUrl;
    anchor.download = qrFileName;
    anchor.click();
  }

  return (
    <main className="page-shell theme-soft">
      <nav className="topbar" aria-label="브랜드">
        <a className="wordmark" href="#top" aria-label="큐알리 홈">
          <span className="brand-dot">
            <QrCode size={18} />
          </span>
          URL2QR
        </a>
        <span className="nav-note">Simple QR maker</span>
      </nav>

      <section className="hero" id="top" aria-labelledby="page-title">
        <div className="intro">
          <p className="eyebrow">FAST · FREE · NO SIGN-UP</p>
          <h1 id="page-title">
            링크를
            <br />한 번에 <em>QR로.</em>
          </h1>
          <p className="lede">
            공유할 주소를 붙여넣으세요. 입력과 동시에 선명한 QR 코드가
            만들어집니다.
          </p>
          <form className="input-area" onSubmit={handleGenerate} noValidate>
            <label htmlFor="url-input">웹 주소</label>
            <div className={`url-control${validationError ? " invalid" : ""}`}>
              <Link2 size={19} aria-hidden="true" />
              <input
                id="url-input"
                type="url"
                value={url}
                placeholder="example.com"
                autoComplete="url"
                spellCheck="false"
                onChange={handleUrlChange}
                onBlur={() => {
                  if (hasValidUrl) setUrl(normalizedUrl);
                }}
              />
              {url && (
                <button
                  type="button"
                  className="clear-button"
                  aria-label="입력 지우기"
                  onClick={() => setUrl("")}
                >
                  <X size={17} />
                </button>
              )}
            </div>
            {displayError ? (
              <p className="message error">{displayError}</p>
            ) : (
              <p className="message">
                https://가 없어도 자동으로 추가해 드려요.
              </p>
            )}
            <button
              type="submit"
              className="download-button generate-button"
              disabled={!hasValidUrl || isGenerating}
            >
              <QrCode size={19} aria-hidden="true" />
              {isGenerating ? "생성하는 중..." : "QR 생성"}
            </button>
          </form>
        </div>

        <div className="preview-card">
          <div className="card-topline">
            <span>YOUR QR</span>
            <span className={`status${qrImageUrl ? " ready" : ""}`}>
              <i /> {isGenerating ? "MAKING" : qrImageUrl ? "READY" : "WAITING"}
            </span>
          </div>
          <div className="qr-stage">
            <span className="corner corner-one" />
            <span className="corner corner-two" />
            <span className="corner corner-three" />
            <span className="corner corner-four" />
            {qrImageUrl ? (
              <img src={qrImageUrl} alt={`${normalizedUrl} QR 코드`} />
            ) : (
              <div className="empty-state">
                <QrCode size={72} strokeWidth={1.25} aria-hidden="true" />
                <span>
                  {isGenerating
                    ? "QR 코드를 만들고 있어요"
                    : requestError
                      ? "서버 연결을 확인해 주세요"
                      : "왼쪽에 주소를 입력해 주세요"}
                </span>
              </div>
            )}
          </div>
          <div className="destination">
            <span>연결 주소</span>
            <p>{hasValidUrl ? normalizedUrl : "아직 입력된 주소가 없어요"}</p>
            {hasValidUrl && <ArrowUpRight size={17} aria-hidden="true" />}
          </div>
          <button
            type="button"
            className="download-button"
            disabled={!canDownload}
            onClick={handleDownload}
          >
            <ArrowDownToLine size={19} aria-hidden="true" />
            {isGenerating ? "생성하는 중..." : "PNG로 저장"}
          </button>
        </div>
      </section>
      <footer>
        <span>01</span>
        <p>URL 입력</p>
        <i />
        <span>02</span>
        <p>QR 확인</p>
        <i />
        <span>03</span>
        <p>이미지 저장</p>
      </footer>
    </main>
  );
}

export default App;
