/* ============================================
   Smart Library Management System
   Service Worker — Offline Support
   ============================================ */

const CACHE_NAME = 'smart-library-v1';
const RUNTIME_CACHE = 'smart-library-runtime-v1';

// Static assets to cache on install
const PRECACHE_URLS = [
    '/',
    '/login',
    '/register',
    '/books',
    '/manifest.json',
    '/images/icon-192.png',
    '/images/icon-512.png'
];

// ---------- INSTALL ----------
// Cache static assets
self.addEventListener('install', event => {
    console.log('📦 Service Worker installing...');
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => {
                console.log('📦 Precaching static assets');
                return cache.addAll(PRECACHE_URLS);
            })
            .then(() => self.skipWaiting())
            .catch(err => console.warn('⚠️ Precaching failed:', err))
    );
});

// ---------- ACTIVATE ----------
// Clean up old caches
self.addEventListener('activate', event => {
    console.log('🚀 Service Worker activating...');
    event.waitUntil(
        caches.keys()
            .then(cacheNames =>
                Promise.all(
                    cacheNames
                        .filter(name => name !== CACHE_NAME && name !== RUNTIME_CACHE)
                        .map(name => {
                            console.log('🗑️ Deleting old cache:', name);
                            return caches.delete(name);
                        })
                )
            )
            .then(() => self.clients.claim())
    );
});

// ---------- FETCH ----------
// Network-first for API, cache-first for static
self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);

    // Skip non-GET and cross-origin
    if (request.method !== 'GET') return;
    if (url.origin !== location.origin) return;

    // Skip API calls — always go to network
    if (url.pathname.startsWith('/api/')) return;

    // Skip actuator and logout
    if (url.pathname.startsWith('/actuator/')) return;
    if (url.pathname === '/logout') return;

    // Network-first for HTML pages
    if (request.headers.get('accept')?.includes('text/html')) {
        event.respondWith(
            fetch(request)
                .then(response => {
                    const cloned = response.clone();
                    caches.open(RUNTIME_CACHE).then(cache => cache.put(request, cloned));
                    return response;
                })
                .catch(() => caches.match(request).then(cached => cached || caches.match('/')))
        );
        return;
    }

    // Cache-first for static assets (CSS, JS, images, fonts)
    event.respondWith(
        caches.match(request)
            .then(cached => {
                if (cached) return cached;
                return fetch(request).then(response => {
                    if (response && response.status === 200) {
                        const cloned = response.clone();
                        caches.open(RUNTIME_CACHE).then(cache => cache.put(request, cloned));
                    }
                    return response;
                });
            })
            .catch(() => {
                // Offline and not cached
                if (request.destination === 'image') {
                    return new Response('', { status: 404 });
                }
                return caches.match('/');
            })
    );
});

// ---------- MESSAGE ----------
self.addEventListener('message', event => {
    if (event.data === 'SKIP_WAITING') {
        self.skipWaiting();
    }
});