import { format, isValid, parseISO } from 'date-fns';

export const formatDate = (iso?: string | null) => {
  if (!iso) return 'N/A';
  const fecha = parseISO(iso);
  return isValid(fecha) ? format(fecha, 'dd/MM/yyyy') : 'N/A';
};

export const formatFechaHora = (iso?: string | null) => {
  if (!iso) return 'N/A';
  const fecha = parseISO(iso);
  return isValid(fecha) ? format(fecha, 'dd/MM/yyyy HH:mm') : 'N/A';
};
