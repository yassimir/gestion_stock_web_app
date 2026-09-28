const CACHE_NAME = "stock-app-v1";

self.addEventListener("install", event => {
    console.log("Service Worker installé");
    self.skipWaiting();
});

self.addEventListener("activate", event => {
    console.log("Service Worker activé");
    event.waitUntil(self.clients.claim());
});