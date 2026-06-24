"use client";

import { useEffect, useState } from "react";
import {
  Wallet,
  createWallet,
  getWallet,
  creditWallet,
  debitWallet,
  transfer,
} from "@/lib/api";
import Modal from "@/components/Modal";
import {
  SendIcon,
  AddIcon,
  WithdrawIcon,
  WalletIcon,
  CheckIcon,
} from "@/components/Icons";

type ActivityKind = "sent" | "added" | "withdrew";
interface Activity {
  id: string;
  kind: ActivityKind;
  amount: number;
  detail: string;
  to?: number; // recipient wallet id (for "sent")
  ts: number;
}

const LS_KEY = "paymesh.walletId";

// Deterministic avatar color from a wallet id (Google palette).
const AVATAR_COLORS = [
  "bg-gblue",
  "bg-gred",
  "bg-ggreen",
  "bg-gyellow",
  "bg-[#9334e6]",
  "bg-[#e8710a]",
];
function avatarColor(id: number) {
  return AVATAR_COLORS[id % AVATAR_COLORS.length];
}

function money(n: number) {
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 2,
  }).format(n || 0);
}

export default function Home() {
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [loading, setLoading] = useState(true);
  const [activity, setActivity] = useState<Activity[]>([]);
  const [toast, setToast] = useState<string | null>(null);

  // setup form
  const [setupId, setSetupId] = useState("");
  const [setupUserId, setSetupUserId] = useState("");
  const [setupErr, setSetupErr] = useState<string | null>(null);
  const [setupBusy, setSetupBusy] = useState(false);

  // action modals
  const [modal, setModal] = useState<null | "send" | "add" | "withdraw">(null);
  const [prefillTo, setPrefillTo] = useState<string>("");

  // quick "pay by wallet id" search bar
  const [search, setSearch] = useState("");

  function openPay(to?: number | string) {
    setPrefillTo(to != null ? String(to) : "");
    setModal("send");
  }

  // Recent recipients derived from this session's transfers (unique, newest first)
  const recipients: number[] = [];
  for (const a of activity) {
    if (a.kind === "sent" && a.to != null && !recipients.includes(a.to)) {
      recipients.push(a.to);
    }
  }

  // Session insights
  const totals = activity.reduce(
    (acc, a) => {
      if (a.kind === "sent") acc.sent += a.amount;
      if (a.kind === "added") acc.added += a.amount;
      if (a.kind === "withdrew") acc.withdrew += a.amount;
      return acc;
    },
    { sent: 0, added: 0, withdrew: 0 }
  );

  useEffect(() => {
    const saved = typeof window !== "undefined" && localStorage.getItem(LS_KEY);
    if (saved) {
      getWallet(Number(saved))
        .then((w) => setWallet(w))
        .catch(() => localStorage.removeItem(LS_KEY))
        .finally(() => setLoading(false));
    } else {
      setLoading(false);
    }
  }, []);

  function showToast(msg: string) {
    setToast(msg);
    setTimeout(() => setToast(null), 3200);
  }

  function logActivity(
    kind: ActivityKind,
    amount: number,
    detail: string,
    to?: number
  ) {
    setActivity((a) => [
      {
        id: Math.random().toString(36).slice(2),
        kind,
        amount,
        detail,
        to,
        ts: Date.now(),
      },
      ...a,
    ]);
  }

  async function refresh(id: number) {
    const w = await getWallet(id);
    setWallet(w);
  }

  async function handleLoad() {
    setSetupErr(null);
    if (!setupId) return setSetupErr("Enter a wallet id");
    setSetupBusy(true);
    try {
      const w = await getWallet(Number(setupId));
      localStorage.setItem(LS_KEY, String(w.id));
      setWallet(w);
    } catch (e: any) {
      setSetupErr("Wallet not found.");
    } finally {
      setSetupBusy(false);
    }
  }

  async function handleCreate() {
    setSetupErr(null);
    if (!setupUserId) return setSetupErr("Enter a user id");
    setSetupBusy(true);
    try {
      const w = await createWallet(Number(setupUserId));
      localStorage.setItem(LS_KEY, String(w.id));
      setWallet(w);
    } catch (e: any) {
      setSetupErr(e.message || "Could not create wallet.");
    } finally {
      setSetupBusy(false);
    }
  }

  function signOut() {
    localStorage.removeItem(LS_KEY);
    setWallet(null);
    setActivity([]);
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center text-gmuted">
        Loading…
      </main>
    );
  }

  if (!wallet) {
    return (
      <Setup
        setupId={setupId}
        setSetupId={setSetupId}
        setupUserId={setupUserId}
        setSetupUserId={setSetupUserId}
        err={setupErr}
        busy={setupBusy}
        onLoad={handleLoad}
        onCreate={handleCreate}
      />
    );
  }

  return (
    <main className="min-h-screen bg-white">
      {/* App bar */}
      <header className="sticky top-0 z-10 bg-white border-b border-gray-100">
        <div className="mx-auto max-w-xl px-4 h-16 flex items-center justify-between">
          <Wordmark />
          <button
            onClick={signOut}
            className="h-10 w-10 rounded-full bg-gsurface text-gtext font-medium flex items-center justify-center hover:shadow-gcard transition-shadow"
            title="Switch wallet"
          >
            {String(wallet.userId).slice(-2)}
          </button>
        </div>
      </header>

      <div className="mx-auto max-w-xl px-4 pb-24 pt-5 space-y-6">
        {/* Pay-by-wallet-id search (GPay signature) */}
        <form
          onSubmit={(e) => {
            e.preventDefault();
            if (search.trim()) {
              openPay(search.trim());
              setSearch("");
            }
          }}
          className="flex items-center gap-3 rounded-full bg-gsurface px-4 py-3"
        >
          <svg viewBox="0 0 24 24" className="h-5 w-5 text-gmuted" fill="currentColor">
            <path d="M15.5 14h-.79l-.28-.27a6.5 6.5 0 1 0-.7.7l.27.28v.79l5 4.99L20.49 19zm-6 0A4.5 4.5 0 1 1 14 9.5 4.5 4.5 0 0 1 9.5 14z" />
          </svg>
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            inputMode="numeric"
            placeholder="Pay by wallet ID"
            className="flex-1 bg-transparent outline-none text-gtext placeholder:text-gmuted"
          />
          {search && (
            <button
              type="submit"
              className="text-sm font-medium text-gblue px-2"
            >
              Pay
            </button>
          )}
        </form>

        {/* Balance card */}
        <section className="rounded-3xl p-6 bg-white border border-gray-200 shadow-gcard">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2 text-gmuted text-sm">
              <span className="h-7 w-7 rounded-full bg-gblue/10 text-gblue grid place-items-center">
                <WalletIcon className="h-4 w-4" />
              </span>
              <span className="font-medium">Paymesh balance</span>
            </div>
            <span
              className={`text-xs rounded-full px-2.5 py-1 font-medium ${
                wallet.isActive
                  ? "bg-ggreen/10 text-ggreen"
                  : "bg-gsurface text-gmuted"
              }`}
            >
              {wallet.isActive ? "Active" : "Inactive"}
            </span>
          </div>

          <div className="mt-4 text-4xl font-semibold tracking-tight text-gtext">
            {money(Number(wallet.balance))}
          </div>

          <div className="mt-6 flex items-center justify-between text-xs text-gmuted">
            <span>Wallet #{wallet.id}</span>
            <span>User {wallet.userId}</span>
          </div>
        </section>

        {/* Quick actions */}
        <section className="grid grid-cols-3 gap-3">
          <Action
            label="Pay"
            color="text-gblue"
            bg="bg-gsurface"
            icon={<SendIcon className="h-6 w-6" />}
            onClick={() => openPay()}
          />
          <Action
            label="Add money"
            color="text-ggreen"
            bg="bg-gsurface"
            icon={<AddIcon className="h-6 w-6" />}
            onClick={() => setModal("add")}
          />
          <Action
            label="Withdraw"
            color="text-gred"
            bg="bg-gsurface"
            icon={<WithdrawIcon className="h-6 w-6" />}
            onClick={() => setModal("withdraw")}
          />
        </section>

        {/* People (recent recipients) */}
        <section>
          <h3 className="text-sm font-medium text-gmuted mb-3 px-1">People</h3>
          <div className="flex gap-4 overflow-x-auto pb-1">
            <button
              onClick={() => openPay()}
              className="flex flex-col items-center gap-2 shrink-0"
            >
              <span className="h-14 w-14 rounded-full border-2 border-dashed border-gray-300 text-gmuted grid place-items-center">
                <AddIcon className="h-6 w-6" />
              </span>
              <span className="text-xs text-gmuted">New</span>
            </button>
            {recipients.map((rid) => (
              <button
                key={rid}
                onClick={() => openPay(rid)}
                className="flex flex-col items-center gap-2 shrink-0"
              >
                <span
                  className={`h-14 w-14 rounded-full grid place-items-center text-white text-lg font-medium ${avatarColor(
                    rid
                  )}`}
                >
                  {String(rid).slice(-2)}
                </span>
                <span className="text-xs text-gtext">#{rid}</span>
              </button>
            ))}
            {recipients.length === 0 && (
              <div className="flex items-center text-sm text-gmuted">
                Pay someone to see them here.
              </div>
            )}
          </div>
        </section>

        {/* Session insights (GPay "Manage your money" style) */}
        <section>
          <h3 className="text-sm font-medium text-gmuted mb-3 px-1">
            This session
          </h3>
          <div className="grid grid-cols-3 gap-3">
            <Insight label="Sent" value={money(totals.sent)} color="text-gblue" />
            <Insight label="Added" value={money(totals.added)} color="text-ggreen" />
            <Insight
              label="Withdrawn"
              value={money(totals.withdrew)}
              color="text-gred"
            />
          </div>
        </section>

        {/* Activity */}
        <section>
          <h3 className="text-sm font-medium text-gmuted mb-2 px-1">
            Recent activity
          </h3>
          {activity.length === 0 ? (
            <div className="rounded-2xl border border-gray-200 p-8 text-center text-gmuted text-sm">
              No activity yet this session.
            </div>
          ) : (
            <ul className="rounded-2xl border border-gray-200 divide-y divide-gray-100 overflow-hidden">
              {activity.map((a) => (
                <ActivityRow key={a.id} a={a} />
              ))}
            </ul>
          )}
        </section>
      </div>

      {/* Send / Pay */}
      <AmountModal
        open={modal === "send"}
        title="Pay"
        cta="Send money"
        ctaClass="bg-gblue"
        withRecipient
        initialTo={prefillTo}
        onClose={() => setModal(null)}
        onSubmit={async ({ amount, toWalletId, description }) => {
          const res = await transfer(
            wallet.id,
            Number(toWalletId),
            amount,
            description || "Transfer"
          );
          await refresh(wallet.id);
          logActivity("sent", amount, `To wallet #${toWalletId}`, Number(toWalletId));
          showToast(`Sent ${money(amount)} · saga #${res.sagaInstanceId}`);
        }}
      />

      {/* Add money / credit */}
      <AmountModal
        open={modal === "add"}
        title="Add money"
        cta="Add to wallet"
        ctaClass="bg-ggreen"
        onClose={() => setModal(null)}
        onSubmit={async ({ amount }) => {
          const w = await creditWallet(wallet.userId, amount);
          setWallet(w);
          logActivity("added", amount, "Top up");
          showToast(`Added ${money(amount)}`);
        }}
      />

      {/* Withdraw / debit */}
      <AmountModal
        open={modal === "withdraw"}
        title="Withdraw"
        cta="Withdraw"
        ctaClass="bg-gred"
        onClose={() => setModal(null)}
        onSubmit={async ({ amount }) => {
          const w = await debitWallet(wallet.userId, amount);
          setWallet(w);
          logActivity("withdrew", amount, "Withdrawal");
          showToast(`Withdrew ${money(amount)}`);
        }}
      />

      {/* Toast */}
      {toast && (
        <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-50 bg-gtext text-white text-sm px-4 py-3 rounded-full shadow-gcard flex items-center gap-2">
          <CheckIcon className="h-4 w-4 text-ggreen" />
          {toast}
        </div>
      )}
    </main>
  );
}

function Action({
  label,
  icon,
  color,
  bg,
  onClick,
}: {
  label: string;
  icon: React.ReactNode;
  color: string;
  bg: string;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="flex flex-col items-center gap-2 py-4 rounded-2xl hover:bg-gsurface/60 transition-colors"
    >
      <span
        className={`h-14 w-14 rounded-full grid place-items-center ${bg} ${color}`}
      >
        {icon}
      </span>
      <span className="text-sm text-gtext">{label}</span>
    </button>
  );
}

function Wordmark() {
  // Google Pay–style multi-color wordmark
  return (
    <div className="flex items-center gap-2">
      <span className="text-2xl font-medium tracking-tight">
        <span className="text-gblue">P</span>
        <span className="text-gred">a</span>
        <span className="text-gyellow">y</span>
        <span className="text-ggreen">m</span>
        <span className="text-gblue">e</span>
        <span className="text-gtext">sh</span>
      </span>
    </div>
  );
}

function Insight({
  label,
  value,
  color,
}: {
  label: string;
  value: string;
  color: string;
}) {
  return (
    <div className="rounded-2xl border border-gray-200 p-3">
      <div className="text-xs text-gmuted">{label}</div>
      <div className={`mt-1 text-base font-medium truncate ${color}`}>
        {value}
      </div>
    </div>
  );
}

function ActivityRow({ a }: { a: Activity }) {
  const map = {
    sent: { sign: "-", color: "text-gred", icon: <SendIcon className="h-5 w-5" />, bg: "bg-gblue/10 text-gblue" },
    added: { sign: "+", color: "text-ggreen", icon: <AddIcon className="h-5 w-5" />, bg: "bg-ggreen/10 text-ggreen" },
    withdrew: { sign: "-", color: "text-gred", icon: <WithdrawIcon className="h-5 w-5" />, bg: "bg-gred/10 text-gred" },
  }[a.kind];
  return (
    <li className="flex items-center gap-3 px-4 py-3 bg-white hover:bg-gsurface/50 transition-colors">
      <span className={`h-10 w-10 rounded-full grid place-items-center ${map.bg}`}>
        {map.icon}
      </span>
      <div className="flex-1 min-w-0">
        <div className="text-gtext capitalize">{a.kind}</div>
        <div className="text-xs text-gmuted truncate">{a.detail}</div>
      </div>
      <div className={`font-medium ${map.color}`}>
        {map.sign}
        {money(a.amount)}
      </div>
    </li>
  );
}

function AmountModal({
  open,
  title,
  cta,
  ctaClass,
  withRecipient,
  initialTo,
  onClose,
  onSubmit,
}: {
  open: boolean;
  title: string;
  cta: string;
  ctaClass: string;
  withRecipient?: boolean;
  initialTo?: string;
  onClose: () => void;
  onSubmit: (v: {
    amount: number;
    toWalletId: string;
    description: string;
  }) => Promise<void>;
}) {
  const [amount, setAmount] = useState("");
  const [toWalletId, setToWalletId] = useState("");
  const [description, setDescription] = useState("");
  const [busy, setBusy] = useState(false);
  const [err, setErr] = useState<string | null>(null);

  // Seed the recipient when the modal opens (e.g. from a People avatar or search).
  useEffect(() => {
    if (open) setToWalletId(initialTo ?? "");
  }, [open, initialTo]);

  function close() {
    setAmount("");
    setToWalletId("");
    setDescription("");
    setErr(null);
    onClose();
  }

  async function submit() {
    setErr(null);
    const amt = Number(amount);
    if (!amt || amt <= 0) return setErr("Enter a valid amount.");
    if (withRecipient && !toWalletId) return setErr("Enter a recipient wallet id.");
    setBusy(true);
    try {
      await onSubmit({ amount: amt, toWalletId, description });
      close();
    } catch (e: any) {
      setErr(e.message || "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal open={open} title={title} onClose={close}>
      <div className="space-y-4">
        <div>
          <label className="text-xs text-gmuted">Amount</label>
          <div className="mt-1 flex items-center rounded-2xl border border-gray-300 focus-within:border-gblue px-4 py-3">
            <span className="text-2xl text-gmuted mr-2">₹</span>
            <input
              autoFocus
              inputMode="decimal"
              value={amount}
              onChange={(e) => setAmount(e.target.value)}
              placeholder="0"
              className="w-full text-2xl outline-none bg-transparent text-gtext"
            />
          </div>
        </div>

        {withRecipient && (
          <>
            <div>
              <label className="text-xs text-gmuted">Recipient wallet id</label>
              <input
                value={toWalletId}
                onChange={(e) => setToWalletId(e.target.value)}
                inputMode="numeric"
                placeholder="e.g. 2"
                className="mt-1 w-full rounded-2xl border border-gray-300 focus:border-gblue px-4 py-3 outline-none"
              />
            </div>
            <div>
              <label className="text-xs text-gmuted">Note (optional)</label>
              <input
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What's it for?"
                className="mt-1 w-full rounded-2xl border border-gray-300 focus:border-gblue px-4 py-3 outline-none"
              />
            </div>
          </>
        )}

        {err && <p className="text-sm text-gred">{err}</p>}

        <button
          onClick={submit}
          disabled={busy}
          className={`w-full rounded-full py-3 text-white font-medium disabled:opacity-60 ${ctaClass}`}
        >
          {busy ? "Processing…" : cta}
        </button>
      </div>
    </Modal>
  );
}

function Setup({
  setupId,
  setSetupId,
  setupUserId,
  setSetupUserId,
  err,
  busy,
  onLoad,
  onCreate,
}: {
  setupId: string;
  setSetupId: (v: string) => void;
  setupUserId: string;
  setSetupUserId: (v: string) => void;
  err: string | null;
  busy: boolean;
  onLoad: () => void;
  onCreate: () => void;
}) {
  return (
    <main className="min-h-screen grid place-items-center bg-white px-4">
      <div className="w-full max-w-md bg-white border border-gray-200 rounded-3xl shadow-glow p-8">
        <div className="text-center mb-6">
          <div className="mx-auto h-14 w-14 rounded-full bg-gblue text-white grid place-items-center mb-3">
            <WalletIcon className="h-7 w-7" />
          </div>
          <h1 className="text-2xl font-medium">
            <span className="text-gblue">P</span>
            <span className="text-gred">a</span>
            <span className="text-gyellow">y</span>
            <span className="text-ggreen">m</span>
            <span className="text-gblue">e</span>
            <span className="text-gtext">sh</span>
          </h1>
          <p className="text-gmuted text-sm mt-1">
            Open your wallet to send and receive money.
          </p>
        </div>

        <div className="space-y-3">
          <label className="text-xs text-gmuted">Open existing wallet</label>
          <div className="flex gap-2">
            <input
              value={setupId}
              onChange={(e) => setSetupId(e.target.value)}
              inputMode="numeric"
              placeholder="Wallet id"
              className="flex-1 rounded-2xl border border-gray-300 focus:border-gblue px-4 py-3 outline-none"
            />
            <button
              onClick={onLoad}
              disabled={busy}
              className="rounded-full bg-gblue text-white px-5 font-medium disabled:opacity-60"
            >
              Open
            </button>
          </div>
        </div>

        <div className="flex items-center gap-3 my-6 text-gmuted text-xs">
          <span className="h-px bg-gray-200 flex-1" />
          OR
          <span className="h-px bg-gray-200 flex-1" />
        </div>

        <div className="space-y-3">
          <label className="text-xs text-gmuted">Create a new wallet</label>
          <div className="flex gap-2">
            <input
              value={setupUserId}
              onChange={(e) => setSetupUserId(e.target.value)}
              inputMode="numeric"
              placeholder="User id"
              className="flex-1 rounded-2xl border border-gray-300 focus:border-gblue px-4 py-3 outline-none"
            />
            <button
              onClick={onCreate}
              disabled={busy}
              className="rounded-full border border-gblue text-gblue px-5 font-medium disabled:opacity-60"
            >
              Create
            </button>
          </div>
        </div>

        {err && <p className="text-sm text-gred mt-4 text-center">{err}</p>}
      </div>
    </main>
  );
}
