import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import Navbar from "./Navbar";
import { useData } from "@/contexts/AppContext";
import { Rol } from "@/types/Rol";

vi.mock("@/contexts/AppContext", () => ({ useData: vi.fn() }));
// El interruptor de tema necesita su propio contexto y aquí no aporta nada
vi.mock("./ThemeToggle", () => ({ ThemeToggle: () => null }));

function renderNavbar(isLogged: boolean, usuario: object | null) {
  vi.mocked(useData).mockReturnValue({ isLogged, usuario } as never);
  render(
    <MemoryRouter>
      <Navbar />
    </MemoryRouter>,
  );
}

describe("Navbar", () => {
  it('con usuario pero sin token no muestra "Mi panel"', () => {
    renderNavbar(false, { name: "Ana", rol: Rol.ROLE_VISITANTE });
    expect(screen.queryByText("Mi panel")).not.toBeInTheDocument();
    expect(screen.getByText("Iniciar sesión")).toBeInTheDocument();
  });

  it('con sesión muestra "Mi panel" y las iniciales', () => {
    renderNavbar(true, { name: "Ana López", rol: Rol.ROLE_VISITANTE });
    expect(screen.getByText("Mi panel")).toBeInTheDocument();
    expect(screen.getByText("AL")).toBeInTheDocument();
    expect(screen.queryByText("Admin")).not.toBeInTheDocument();
  });

  it("el enlace Admin solo aparece para ROLE_ADMIN", () => {
    renderNavbar(true, { name: "Root", rol: Rol.ROLE_ADMIN });
    expect(screen.getByText("Admin")).toBeInTheDocument();
  });
});
