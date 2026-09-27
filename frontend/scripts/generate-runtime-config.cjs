const fs = require("node:fs");
const path = require("node:path");

const projectRoot = path.resolve(__dirname, "..");
const envPath = path.join(projectRoot, ".env");

function readEnvFile(filePath) {
  if (!fs.existsSync(filePath)) return {};

  return fs
    .readFileSync(filePath, "utf8")
    .split(/\r?\n/)
    .reduce((env, line) => {
      let trimmed = line.trim();
      if (!trimmed || trimmed.startsWith("#")) return env;

      if (trimmed.startsWith("export ")) {
        trimmed = trimmed.slice("export ".length).trimStart();
      }

      const equalsIndex = trimmed.indexOf("=");
      if (equalsIndex < 0) return env;

      const key = trimmed.slice(0, equalsIndex).trim();
      if (!/^[A-Za-z_]\w*$/.test(key)) return env;

      let value = trimmed.slice(equalsIndex + 1).trim();
      if (
        (value.startsWith('"') && value.endsWith('"')) ||
        (value.startsWith("'") && value.endsWith("'"))
      ) {
        value = value.slice(1, -1);
      } else {
        for (let i = 1; i < value.length; i += 1) {
          if (value[i] === "#" && /\s/.test(value[i - 1])) {
            value = value.slice(0, i).trim();
            break;
          }
        }
      }

      env[key] = value;
      return env;
    }, {});
}

const fileEnv = readEnvFile(envPath);

const outputFlagIndex = process.argv.indexOf("--output");
const outputPath =
  outputFlagIndex >= 0
    ? process.argv[outputFlagIndex + 1]
    : "public/runtime-config.js";

if (!outputPath) {
  throw new Error("--output 다음에 출력 경로를 지정해야 합니다.");
}

const runtimeConfig = {
  VITE_QR_API_BASE_URL:
    process.env.VITE_QR_API_BASE_URL ||
    fileEnv.VITE_QR_API_BASE_URL ||
    "http://localhost:8080",
};

const absoluteOutputPath = path.resolve(projectRoot, outputPath);
fs.mkdirSync(path.dirname(absoluteOutputPath), { recursive: true });
fs.writeFileSync(
  absoluteOutputPath,
  `window.__RUNTIME_CONFIG__ = ${JSON.stringify(runtimeConfig)};\n`,
  "utf8",
);

console.log(`Runtime config written to ${absoluteOutputPath}`);
console.log(
  fs.existsSync(envPath) ? `Loaded ${envPath}` : `No .env found at ${envPath}`,
);
