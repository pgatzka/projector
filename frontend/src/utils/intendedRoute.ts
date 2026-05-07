const KEY = "projector.intendedRoute";

export const stashIntendedRoute = (path: string) => sessionStorage.setItem(KEY, path);

export const popIntendedRoute = (): string | null => {
  const v = sessionStorage.getItem(KEY);
  if (v) sessionStorage.removeItem(KEY);
  return v;
};
