# Paymesh Pay — client

A Google Pay–style Next.js client for the **PaymeshLedger** Spring Boot backend.
It supports exactly the functionality exposed by the backend today:

| Action      | Backend endpoint                              |
| ----------- | --------------------------------------------- |
| Create wallet | `POST /wallets` `{ userId }`                |
| Open wallet / balance | `GET /wallets/{id}`                 |
| Add money (credit) | `POST /wallets/{userId}/credit` `{ amount }` |
| Withdraw (debit)   | `POST /wallets/{userId}/debit` `{ amount }`  |
| Pay / transfer     | `POST /transactions` `{ fromWalletId, toWalletId, amount, description }` |

> The backend has no "list transactions" endpoint, so the **Recent activity**
> feed shows actions performed in the current browser session only.

## Run

```bash
cd client
npm install
npm run dev
```

Open http://localhost:3000.

The backend is expected at `http://localhost:8080`. Requests are proxied via a
Next.js rewrite (`/api/*` → backend) to avoid CORS. Override with:

```bash
BACKEND_URL=http://my-host:8080 npm run dev
```
