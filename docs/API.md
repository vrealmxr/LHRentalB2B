# b2b JSON API

Backend source lives on the server at
`domains/lhrental.gr/public_html/b2b/api/` (not in this repo — it's PHP
bolted onto the existing legacy b2b app, deployed by hand over SSH; see the
"Deploying backend changes" section below). Base URL:

```
https://lhrental.gr/b2b/api/
```

It sits next to the existing procedural PHP portal and reuses its database
and business logic (`imports/functions.php`, `imports/sql_settings.php`) —
it does not replace anything, it adds a JSON layer for the Android app to
call instead of scraping HTML or reusing session cookies.

## Auth

Bearer tokens, not the web portal's session cookies. `api_tokens` table
(`token_hash` — sha256 of the raw token, never stored in plaintext) is new;
nothing else was touched.

```
POST auth/login.php      { username, password }  -> { token, expires_at, user }
POST auth/logout.php     (Bearer)                 -> { logged_out: true }
GET  auth/me.php         (Bearer)                 -> { user }
```

Every other endpoint below requires `Authorization: Bearer <token>` except
`categories/index.php` and `products/*`, which are public (same content the
public catalogue page shows logged-out).

## Catalogue

```
GET categories/index.php?lang=gr|en|es|ar
GET products/index.php?lang=..&category_id=&q=&page=&per_page=
GET products/show.php?id=&lang=..
```

`category_id` must be a **leaf** category id (one with no children) —
`product_categorie` in the products table is never a parent id. The Android
client flattens the tree client-side before offering filter chips
(`CatalogViewModel.filterableCategories`).

## Orders

```
GET  orders/index.php               (Bearer) -> { orders: [...] }        (current user only)
GET  orders/show.php?id=            (Bearer) -> { order: {..., items} }  (current user only)
POST orders/create.php              (Bearer) -> { order: { id, total } }
```

`orders/create.php` re-prices every line from the `products` table server
side — it does not trust a client-submitted price, unlike the legacy
`imports/addorder.php` it's modeled on. Same turnover/discount-tier update
(`users.tziros`, `users.off`) as that file, via prepared statements instead
of string-concatenated SQL.

`order_stage` values (confirmed against `admin-legacy/orders.php` and
`admin-legacy/order.php`'s own badges): `0` = not verified, `1` = verified,
`2` = delivered.

## Invoices

```
GET invoices/index.php              (Bearer) -> { invoices: [...] }
GET invoices/download.php?id=       (Bearer) -> raw PDF bytes
```

`invoices/download.php` exists because the web account page
(`imports/invoices.php`) has **no user_id filter and no auth at all** — it
lists every customer's invoices and links straight to
`b2b/files/<name>.pdf`, unauthenticated. The API endpoint is the fix for
this surface: it checks `WHERE id = ? AND user_id = ?` before streaming
anything. Worth fixing on the web side too, independent of this app.

## Known gaps / next steps

- No token refresh — a token is valid 90 days from login, then the user has
  to sign in again. Fine for v1, worth a refresh endpoint before this feels
  finished.
- No rate limiting on `auth/login.php`.
- Token is stored in plain DataStore on the Android side, not
  EncryptedSharedPreferences / Keystore-backed. Same trust boundary as any
  other app-private file, but worth hardening before a production release.
- Cart is in-memory only (`CartStore`) — killed on process death. Acceptable
  for a quote/booking-request flow, not for anything transactional.

## Deploying backend changes

There's no git repo for `b2b/` on the server yet — changes were made
directly over SSH (see the session that built this). If this API grows
past a handful of files, that folder should get its own repo before the
next change, the same way this Android app has one.
