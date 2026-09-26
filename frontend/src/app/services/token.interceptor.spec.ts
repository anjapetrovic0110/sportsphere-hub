import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { tokenInterceptor } from './token.interceptor';
import { API_URL } from './api.config';

describe('API authentication interceptor', () => {
  let http: HttpClient;
  let requests: HttpTestingController;
  beforeEach(() => {
    localStorage.removeItem('prijavljeniKorisnik');
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withInterceptors([tokenInterceptor])), provideHttpClientTesting()
    ] });
    http = TestBed.inject(HttpClient);
    requests = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    requests.verify();
    localStorage.removeItem('prijavljeniKorisnik');
  });
  function login(exp: number) {
    const token = `header.${btoa(JSON.stringify({ exp }))}.signature`;
    localStorage.setItem('prijavljeniKorisnik', JSON.stringify({ token, uloga: 'ATHLETE' }));
    return token;
  }
  it('adds a valid token and translates API payloads', () => {
    const token = login(Math.floor(Date.now() / 1000) + 120);
    http.post(`${API_URL}/sportista/profil`, { imejl: 'ana@example.test' }).subscribe(body => {
      expect(body).toEqual({ imejl: 'ana@example.test' });
    });
    const req = requests.expectOne(`${API_URL}/sportista/profil`);
    expect(req.request.headers.get('Authorization')).toBe(`Bearer ${token}`);
    expect(req.request.body).toEqual({ email: 'ana@example.test' });
    req.flush({ email: 'ana@example.test' });
  });
  it('does not send credentials or rewrite requests to third parties', () => {
    login(Math.floor(Date.now() / 1000) + 120);
    http.post('https://external.example/data', { imejl: 'ana@example.test' }).subscribe();
    const req = requests.expectOne('https://external.example/data');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    expect(req.request.body).toEqual({ imejl: 'ana@example.test' });
    req.flush({});
  });
  for (const storage of ['invalid JSON', JSON.stringify({ token: 'bad' })]) {
    it(`recovers from invalid stored credentials: ${storage}`, () => {
      localStorage.setItem('prijavljeniKorisnik', storage);
      http.get(`${API_URL}/javno/status`).subscribe();
      const req = requests.expectOne(`${API_URL}/javno/status`);
      expect(req.request.headers.has('Authorization')).toBeFalse();
      expect(localStorage.getItem('prijavljeniKorisnik')).toBeNull();
      req.flush({});
    });
  }
  it('removes expired credentials', () => {
    login(1);
    http.get(`${API_URL}/javno/status`).subscribe();
    const req = requests.expectOne(`${API_URL}/javno/status`);
    expect(req.request.headers.has('Authorization')).toBeFalse();
    expect(localStorage.getItem('prijavljeniKorisnik')).toBeNull();
    req.flush({});
  });
});
