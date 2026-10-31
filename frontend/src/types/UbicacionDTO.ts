import { EventoDTO } from './EventoDTO';

export interface UbicacionDTO {
  id: number;
  nombre: string;
  latitud: number;
  longitud: number;
  eventos?: EventoDTO[];
}
