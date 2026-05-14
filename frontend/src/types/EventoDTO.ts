import { UbicacionDTO } from "./UbicacionDTO";
import { ResenaDTO } from "./ResenaDTO";

export interface EventoDTO {
  id?: number;
  nombre: string;
  descripcion?: string;
  fecha: string;
  ubicacion: UbicacionDTO;
  imagenUrl?: string;
  categoriaId?: number;
  organizadorId: number;
  resenas?: ResenaDTO[];
}
