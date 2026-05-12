import { useParams } from "react-router-dom";
import { EventoService } from "../services/eventoService";
import { toast } from "sonner";
import { useEffect, useState, useRef } from "react";
import { EventoDTO } from "../types/EventoDTO";
import { Badge } from "@/components/ui/badge";
import { MapContainer, TileLayer, Marker, Popup } from "react-leaflet";
import "leaflet/dist/leaflet.css";
import L from "leaflet";
import iconUrl from "leaflet/dist/images/marker-icon.png";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import { useTheme } from "@/components/theme-provider";

const defaultIcon = L.icon({
  iconUrl,
  shadowUrl: iconShadow,
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
});
L.Marker.prototype.options.icon = defaultIcon;

const EventPage = () => {
  const { id } = useParams<{ id: string }>();
  const { theme } = useTheme();
  const [event, setEvent] = useState<EventoDTO | null>(null);
  const [loading, setLoading] = useState(false);
  const lat = event?.ubicacion.latitud;
  const lng = event?.ubicacion.longitud;
  const posicionValida = lat !== undefined && lng !== undefined;

  const darkIcon =  L.icon({
    iconUrl: '/dark-map-icon.png',
    shadowUrl: iconShadow,
    iconSize: [35, 35],
    iconAnchor: [17, 41],
    popupAnchor: [1, -34],
  });

  const mapRef = useRef<L.Map | null>(null);

  const tileUrl = theme === 'dark'
    ? "https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
    : "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

  const centrarMapa = () => {
    if (mapRef.current && posicionValida) {
      mapRef.current.flyTo([lat, lng], 15);
    }
  };

  useEffect(() => {
    const loadEvent = async () => {
      if (!id) return;

      setLoading(true);
      try {
        const data = await EventoService.obtenerPorId(Number(id));
        setEvent(data);
      } catch (error) {
        console.error("Error al cargar el evento", error);
        toast.error("No se pudo cargar el evento.");
      } finally {
        setLoading(false);
      }
    };

    loadEvent();
  }, [id]);

  useEffect(() => {
    if (mapRef.current && posicionValida) {
      const timeout = setTimeout(() => {
        mapRef.current?.invalidateSize();
      }, 250);

      return () => clearTimeout(timeout);
    }
  }, [posicionValida, event, theme]);

  if (loading) return <div>Cargando evento...</div>;
  if (!event) return <div>Evento no encontrado.</div>;

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold">{event.nombre}</h1>
      <img
        src={event.imagenUrl}
        alt={event.nombre}
        className="w-full h-64 object-cover mt-4 rounded-lg"
      />
      <p className="mt-4">{event.descripcion}</p>
      <p className="mt-2 font-semibold">
        Ubicación: <Badge>{event.ubicacion.nombre}</Badge>
      </p>
      <p className="mt-2">
        Fecha: {new Date(event.fecha).toLocaleDateString()}
      </p>

      {posicionValida ? (
        <div className="mt-8">
          <div className="flex justify-between items-center mb-2">
            <p className="font-semibold">Ubicación en el mapa:</p>
            <button
              onClick={centrarMapa}
              className="bg-blue-600 hover:bg-blue-700 text-white px-3 py-1 rounded text-sm transition-colors"
            >
              Volver al punto central
            </button>
          </div>
          <div className="h-96 w-full rounded-lg overflow-hidden border relative">
            <MapContainer
              center={[lat, lng]}
              zoom={15}
              scrollWheelZoom={false}
              style={{ height: "100%", width: "100%" }}
              ref={mapRef}
            >
              <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
                url={tileUrl}
              />

              <Marker position={[lat, lng]} icon={ theme === 'light' ? defaultIcon : darkIcon }>
                <Popup>
                  <div className="bg-primary text-primary-foreground p-2 rounded-md -m-1">
                    {event.ubicacion.nombre}
                  </div>
                </Popup>
              </Marker>
            </MapContainer>
          </div>
        </div>
      ) : (
        <p className="text-red-500 mt-4">
          La ubicación no tiene coordenadas exactas para mostrar en el mapa.
        </p>
      )}
    </div>
  );
};

export default EventPage;
