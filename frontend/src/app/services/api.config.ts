const config = (window as Window & { sportsphereConfig?: { apiUrl?: string } }).sportsphereConfig;
export const API_URL = (config?.apiUrl ?? '').replace(/\/+$/, '');

export function isApiRequest(url: string): boolean {
  const base = new URL(API_URL || '/', window.location.origin);
  const target = new URL(url, window.location.origin);
  const path = base.pathname.replace(/\/+$/, '');
  return target.origin === base.origin &&
    (target.pathname === path || target.pathname.startsWith(`${path}/`));
}
