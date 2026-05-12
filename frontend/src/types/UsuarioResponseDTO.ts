import { Rol } from "./Rol";

export interface UsuarioResponseDTO {
  id: string;
  nombre: string;
  email: string;
  rol: Rol;
  fechaRegistro: string;
}
