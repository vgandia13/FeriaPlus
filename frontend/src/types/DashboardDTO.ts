import { EventoDTO } from './EventoDTO';
import { ResenaDTO } from './ResenaDTO';
import { UsuarioResponseDTO } from './UsuarioResponseDTO';

export interface DashboardDTO {
  usuario: UsuarioResponseDTO;
  resenas?: ResenaDTO[] | [];
  reservas?:
    | Array<{
        id: number;
        fechaReserva: string;
        estado: string;
        expositorId: number;
        puestoId: number;
      }>
    | [];
  eventosAsistidos?: EventoDTO[] | [];
  puestos?:
    | Array<{
        id: number;
        nombre: string;
        descripcion: string;
        precio: number;
      }>
    | [];
}
