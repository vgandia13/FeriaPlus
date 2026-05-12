import { UbicacionDTO } from "./UbicacionDTO";

export interface EventoDTO {
  id?: number;
  nombre: string;
  descripcion?: string;
  fecha: string;
  ubicacion: UbicacionDTO;
  imagenUrl?: string;
  categoriaId?: number;
  organizadorId: number;
}
