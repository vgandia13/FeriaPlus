import { EventoDTO } from "./EventoDTO";

export interface UbicacionDTO {
  id: string;
  nombre: string;
  latitud: number;
  longitud: number;
  eventos: EventoDTO[];
}
