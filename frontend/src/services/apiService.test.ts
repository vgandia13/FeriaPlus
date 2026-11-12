import { describe, it, expect, vi, beforeEach } from 'vitest';
import {
  AxiosError,
  type AxiosResponse,
  type InternalAxiosRequestConfig,
} from 'axios';
import api, { setAuthCallbacks } from './apiService';

// Adaptador falso: en vez de llamar al backend, toda petición responde 401
const responde401 = async (config: InternalAxiosRequestConfig) => {
  const response = {
    status: 401,
    statusText: '',
    data: {},
    headers: {},
    config,
  } as AxiosResponse;
  throw new AxiosError(
    '401',
    AxiosError.ERR_BAD_REQUEST,
    config,
    null,
    response,
  );
};

describe('interceptor de 401', () => {
  const setIsLogged = vi.fn();
  const setUsuario = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.setItem('token', 'abc');
    setAuthCallbacks(setIsLogged, setUsuario);
  });

  it('un 401 en /auth/login no cierra la sesión', async () => {
    await expect(
      api.post('/auth/login', {}, { adapter: responde401 }),
    ).rejects.toThrow();
    expect(localStorage.getItem('token')).toBe('abc');
    expect(setIsLogged).not.toHaveBeenCalled();
  });

  it('un 401 en otra ruta borra el token y cierra la sesión', async () => {
    await expect(
      api.get('/perfil/me', { adapter: responde401 }),
    ).rejects.toThrow();
    expect(localStorage.getItem('token')).toBeNull();
    expect(setIsLogged).toHaveBeenCalledWith(false);
    expect(setUsuario).toHaveBeenCalledWith(null);
  });
});
