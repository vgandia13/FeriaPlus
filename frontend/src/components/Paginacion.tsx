import { Button } from "@/components/ui/button";
import {
  ChevronLeft,
  ChevronRight,
  ChevronsLeft,
  ChevronsRight,
} from "lucide-react";

interface PaginacionProps {
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export function Paginacion({
  page,
  totalPages,
  onPageChange,
}: PaginacionProps) {
  const ultima = Math.max(0, totalPages - 1);
  const esPrimera = page <= 0;
  const esUltima = page >= ultima;

  return (
    <nav
      aria-label="Paginación"
      className="flex items-center justify-center gap-1"
    >
      <Button
        variant="ghost"
        size="icon"
        aria-label="Primera página"
        disabled={esPrimera}
        onClick={() => onPageChange(0)}
      >
        <ChevronsLeft />
      </Button>
      <Button
        variant="ghost"
        disabled={esPrimera}
        onClick={() => onPageChange(page - 1)}
      >
        <ChevronLeft /> Anterior
      </Button>

      <span className="mx-4 text-sm font-medium" aria-live="polite">
        Página {page + 1} de {totalPages || 1}
      </span>

      <Button
        variant="ghost"
        disabled={esUltima}
        onClick={() => onPageChange(page + 1)}
      >
        Siguiente <ChevronRight />
      </Button>
      <Button
        variant="ghost"
        size="icon"
        aria-label="Última página"
        disabled={esUltima}
        onClick={() => onPageChange(ultima)}
      >
        <ChevronsRight />
      </Button>
    </nav>
  );
}
