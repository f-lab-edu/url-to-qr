import React, { useMemo, useState } from "react";
import { ArrowDownToLine, ArrowUpRight, Link2, QrCode, X } from "lucide-react";

const QR_API_BASE_URL = import.meta.env.VITE_QR_API_BASE_URL || "http://localhost:8080";

function normalizeUrl(value) {
  const trimmed = value.trim();
  if (!trimmed) return "";
  return /^[a-z][a-z\d+\-.]*:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;
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

function createQrApiUrl(text) {
  return `${QR_API_BASE_URL}/api/qr?${new URLSearchParams({ text })}`;
}

function App() {
  const [url, setUrl] = useState("");
  const [imageError, setImageError] = useState(false);
  const [downloadError, setDownloadError] = useState("");
  const [isDownloading, setIsDownloading] = useState(false);

  const normalizedUrl = useMemo(() => normalizeUrl(url), [url]);
  const hasInput = Boolean(url.trim());
  const hasValidUrl = isValidUrl(normalizedUrl);
  const qrApiUrl = hasValidUrl ? createQrApiUrl(normalizedUrl) : "";
  const validationError = hasInput && !hasValidUrl ? "올바른 URL을 입력해 주세요." : "";
  const displayError = validationError || downloadError;
  const canDownload = Boolean(qrApiUrl && !imageError);

  function handleUrlChange(event) {
    setUrl(event.target.value);
    setImageError(false);
    setDownloadError("");
  }

  function handleReset() {
    setUrl("");
    setImageError(false);
    setDownloadError("");
  }

  async function handleDownload() {
    if (!canDownload || isDownloading) return;
    setIsDownloading(true);
    setDownloadError("");
    try {
      const response = await fetch(qrApiUrl);
      if (!response.ok) throw new Error();
      const objectUrl = URL.createObjectURL(await response.blob());
      const anchor = document.createElement("a");
      anchor.href = objectUrl;
      anchor.download = "qr-code.png";
      anchor.click();
      URL.revokeObjectURL(objectUrl);
    } catch {
      setDownloadError("서버에서 QR 코드를 가져오지 못했습니다.");
    } finally {
      setIsDownloading(false);
    }
  }

  return (
    <main className="page-shell theme-soft">
      <nav className="topbar" aria-label="브랜드">
        <a className="wordmark" href="#top" aria-label="큐알리 홈">
          <span className="brand-dot"><QrCode size={18} /></span>
          Qrly
        </a>
        <span className="nav-note">Simple QR maker</span>
      </nav>

      <section className="hero" id="top" aria-labelledby="page-title">
        <div className="intro">
          <p className="eyebrow">FAST · FREE · NO SIGN-UP</p>
          <h1 id="page-title">링크를<br />한 번에 <em>QR로.</em></h1>
          <p className="lede">공유할 주소를 붙여넣으세요. 입력과 동시에 선명한 QR 코드가 만들어집니다.</p>

          <form className="input-area" onSubmit={(event) => event.preventDefault()}>
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
              />
              {url && <button type="button" className="clear-button" aria-label="입력 지우기" onClick={handleReset}><X size={17} /></button>}
            </div>
            {displayError ? <p className="message error">{displayError}</p> : <p className="message">https://가 없어도 자동으로 추가해 드려요.</p>}
          </form>
        </div>

        <div className="preview-card">
          <div className="card-topline">
            <span>YOUR QR</span>
            <span className={`status${qrApiUrl && !imageError ? " ready" : ""}`}>
              <i /> {qrApiUrl && !imageError ? "READY" : "WAITING"}
            </span>
          </div>
          <div className="qr-stage">
            <span className="corner corner-one" /><span className="corner corner-two" />
            <span className="corner corner-three" /><span className="corner corner-four" />
            {qrApiUrl && !imageError ? (
              <img key={qrApiUrl} src={qrApiUrl} alt={`${normalizedUrl} QR 코드`} onError={() => setImageError(true)} />
            ) : (
              <div className="empty-state">
                <QrCode size={72} strokeWidth={1.25} aria-hidden="true" />
                <span>{imageError ? "서버 연결을 확인해 주세요" : "왼쪽에 주소를 입력해 주세요"}</span>
              </div>
            )}
          </div>
          <div className="destination">
            <span>연결 주소</span>
            <p>{hasValidUrl ? normalizedUrl : "아직 입력된 주소가 없어요"}</p>
            {hasValidUrl && <ArrowUpRight size={17} aria-hidden="true" />}
          </div>
          <button type="button" className="download-button" disabled={!canDownload || isDownloading} onClick={handleDownload}>
            <ArrowDownToLine size={19} aria-hidden="true" />
            {isDownloading ? "저장하는 중..." : "PNG로 저장"}
          </button>
        </div>
      </section>

      <footer><span>01</span><p>URL 입력</p><i /><span>02</span><p>QR 확인</p><i /><span>03</span><p>이미지 저장</p></footer>
    </main>
  );
}

export default App;
