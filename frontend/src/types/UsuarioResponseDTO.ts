import { Rol } from './Rol';

export interface UsuarioResponseDTO {
  id: number;
  nombre: string;
  email: string;
  rol: Rol;
  fechaRegistro: string;
}
