import js from "@eslint/js";
import globals from "globals";
import reactHooks from "eslint-plugin-react-hooks";
import reactRefresh from "eslint-plugin-react-refresh";
import tseslint from "typescript-eslint";
import { defineConfig, globalIgnores } from "eslint/config";

export default defineConfig([
  globalIgnores(["dist"]),
  {
    files: ["**/*.{ts,tsx}"],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      "react-refresh/only-export-components": "off", // Adiós al error "estúpido"
      "@typescript-eslint/no-unused-vars": "warn", // Que no te bloquee por una variable que vas a usar luego
      "no-console": ["warn", { allow: ["error"] }],
    },
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser, // Para no tener que importar React en cada archivo
    },
  },
]);
