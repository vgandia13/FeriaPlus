export interface UsuarioActualizacion {
  nombre: string;
  email: string;
  oldPassword?: string;
  newPassword?: string;
}
