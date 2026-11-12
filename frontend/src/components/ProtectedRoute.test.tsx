import { describe, it, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import { useData } from "../contexts/AppContext";
import { Rol } from "../types/Rol";

// Sustituimos el contexto real: cada test decide si hay sesión y qué usuario
vi.mock("../contexts/AppContext", () => ({ useData: vi.fn() }));

function renderEn(ruta: string, allowedRoles?: Rol[]) {
  render(
    <MemoryRouter initialEntries={[ruta]}>
      <Routes>
        <Route path="/" element={<p>Inicio</p>} />
        <Route path="/login" element={<p>Login</p>} />
        <Route element={<ProtectedRoute allowedRoles={allowedRoles} />}>
          <Route path="/privada" element={<p>Privada</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

describe("ProtectedRoute", () => {
  it("sin sesión redirige a /login", () => {
    vi.mocked(useData).mockReturnValue({
      isLogged: false,
      usuario: null,
    } as never);
    renderEn("/privada");
    expect(screen.getByText("Login")).toBeInTheDocument();
  });

  it("con token pero sin usuario, en una ruta con rol, redirige a /", () => {
    vi.mocked(useData).mockReturnValue({
      isLogged: true,
      usuario: null,
    } as never);
    renderEn("/privada", [Rol.ROLE_ADMIN]);
    expect(screen.getByText("Inicio")).toBeInTheDocument();
  });

  it("con el rol permitido muestra la ruta", () => {
    vi.mocked(useData).mockReturnValue({
      isLogged: true,
      usuario: { rol: Rol.ROLE_ADMIN },
    } as never);
    renderEn("/privada", [Rol.ROLE_ADMIN]);
    expect(screen.getByText("Privada")).toBeInTheDocument();
  });
});
