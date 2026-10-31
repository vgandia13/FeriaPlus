import { Switch } from "@/components/ui/switch";
import { Moon, Sun } from "lucide-react";
import { useTheme } from "@/components/theme-provider";

export function ThemeToggle() {
  const { resolvedTheme, setTheme } = useTheme();

  const isDark = resolvedTheme === "dark";

  return (
    <div className="flex items-center space-x-2 px-2">
      <Sun
        className={`h-4 w-4 ${!isDark ? "text-secondary" : "text-muted-foreground"}`}
      />

      <Switch
        id="theme-mode"
        aria-label="Modo oscuro"
        checked={isDark}
        size="default"
        onCheckedChange={(checked) => setTheme(checked ? "dark" : "light")}
      />

      <Moon
        className={`h-4 w-4 ${isDark ? "text-secondary" : "text-muted-foreground"}`}
      />
    </div>
  );
}
