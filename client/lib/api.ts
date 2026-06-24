// Thin client over the PaymeshLedger Spring Boot backend.
// Calls go through the /api/* rewrite (see next.config.mjs) to avoid CORS.

export interface Wallet {
  id: number;
  userId: number;
  isActive: boolean;
  balance: number;
}

export interface TransferResponse {
  sagaInstanceId: number;
}

async function handle<T>(res: Response): Promise<T> {
  if (!res.ok) {
    let msg = `Request failed (${res.status})`;
    try {
      const body = await res.text();
      if (body) msg = body;
    } catch {
      /* ignore */
    }
    throw new Error(msg);
  }
  const text = await res.text();
  return (text ? JSON.parse(text) : null) as T;
}

const json = { "Content-Type": "application/json" };

// POST /wallets  { userId }
export function createWallet(userId: number): Promise<Wallet> {
  return fetch("/api/wallets", {
    method: "POST",
    headers: json,
    body: JSON.stringify({ userId }),
  }).then((r) => handle<Wallet>(r));
}

// GET /wallets/{id}
export function getWallet(id: number): Promise<Wallet> {
  return fetch(`/api/wallets/${id}`).then((r) => handle<Wallet>(r));
}

// GET /wallets/{id}/balance
export function getBalance(id: number): Promise<number> {
  return fetch(`/api/wallets/${id}/balance`).then((r) => handle<number>(r));
}

// POST /wallets/{userId}/credit  { amount }   (Add money)
export function creditWallet(userId: number, amount: number): Promise<Wallet> {
  return fetch(`/api/wallets/${userId}/credit`, {
    method: "POST",
    headers: json,
    body: JSON.stringify({ amount }),
  }).then((r) => handle<Wallet>(r));
}

// POST /wallets/{userId}/debit  { amount }    (Withdraw)
export function debitWallet(userId: number, amount: number): Promise<Wallet> {
  return fetch(`/api/wallets/${userId}/debit`, {
    method: "POST",
    headers: json,
    body: JSON.stringify({ amount }),
  }).then((r) => handle<Wallet>(r));
}

// POST /transactions  { fromWalletId, toWalletId, amount, description }  (Send)
export function transfer(
  fromWalletId: number,
  toWalletId: number,
  amount: number,
  description: string
): Promise<TransferResponse> {
  return fetch("/api/transactions", {
    method: "POST",
    headers: json,
    body: JSON.stringify({ fromWalletId, toWalletId, amount, description }),
  }).then((r) => handle<TransferResponse>(r));
}
