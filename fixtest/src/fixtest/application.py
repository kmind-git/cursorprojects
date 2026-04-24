from __future__ import annotations

from dataclasses import dataclass
from typing import Dict, Optional

import quickfix as fix


@dataclass
class SessionState:
    session_id: Optional[fix.SessionID] = None
    logged_on: bool = False


def _msgtype(message: fix.Message) -> str:
    header = message.getHeader()
    return header.getField(fix.MsgType().getField())


def _safe_str(message: fix.Message) -> str:
    # QuickFIX Message.__str__ returns tag-value with SOH separators.
    # Avoid leaking passwords if tags are configured.
    raw = str(message)
    return raw.replace("\x01", "|")


class FixApplication(fix.Application):
    """
    Minimal QuickFIX Application implementation.

    - Injects custom Logon tags from SessionSettings:
      keys like `LogonTag.<tag>=<value>` are appended to MsgType=A (Logon).
    """

    def __init__(self, *, logon_tags: Optional[Dict[int, str]] = None):
        super().__init__()
        self._logon_tags = dict(logon_tags or {})
        self.state = SessionState()

    # ---- Session lifecycle ----
    def onCreate(self, sessionID: fix.SessionID) -> None:  # noqa: N802
        self.state.session_id = sessionID
        print(f"[onCreate] {sessionID.toString()}")

    def onLogon(self, sessionID: fix.SessionID) -> None:  # noqa: N802
        self.state.session_id = sessionID
        self.state.logged_on = True
        print(f"[onLogon] {sessionID.toString()}")

    def onLogout(self, sessionID: fix.SessionID) -> None:  # noqa: N802
        self.state.session_id = sessionID
        self.state.logged_on = False
        print(f"[onLogout] {sessionID.toString()}")

    # ---- Admin messages ----
    def toAdmin(self, message: fix.Message, sessionID: fix.SessionID) -> None:  # noqa: N802
        try:
            mt = _msgtype(message)
        except Exception:
            mt = "?"

        if mt == fix.MsgType_Logon:
            self._inject_logon_tags(message, sessionID)

        print(f"[toAdmin:{mt}] {sessionID.toString()} {self._format_msg(message)}")

    def fromAdmin(self, message: fix.Message, sessionID: fix.SessionID) -> None:  # noqa: N802
        mt = _msgtype(message)
        print(f"[fromAdmin:{mt}] {sessionID.toString()} {self._format_msg(message)}")

    # ---- App messages ----
    def toApp(self, message: fix.Message, sessionID: fix.SessionID) -> None:  # noqa: N802
        mt = _msgtype(message)
        print(f"[toApp:{mt}] {sessionID.toString()} {self._format_msg(message)}")

    def fromApp(self, message: fix.Message, sessionID: fix.SessionID) -> None:  # noqa: N802
        mt = _msgtype(message)
        print(f"[fromApp:{mt}] {sessionID.toString()} {self._format_msg(message)}")

    # ---- Helpers ----
    def _format_msg(self, message: fix.Message) -> str:
        # Mask common password tags if present.
        s = _safe_str(message)
        # 554=Password (FIX standard), 925=NewPassword, 96=RawData
        for tag in ("554", "925", "96"):
            s = self._mask_tag(s, tag)
        return s

    @staticmethod
    def _mask_tag(msg: str, tag: str) -> str:
        # msg uses '|' separators
        needle = f"{tag}="
        parts = msg.split("|")
        for i, p in enumerate(parts):
            if p.startswith(needle) and len(p) > len(needle):
                parts[i] = needle + "***"
        return "|".join(parts)

    def _inject_logon_tags(self, message: fix.Message, sessionID: fix.SessionID) -> None:
        """
        Set custom tags on Logon (35=A).

        Tags are loaded by the app entrypoint from `fix_client.cfg` keys like:
        - LogonTag.553=myuser
        - LogonTag.554=mypassword
        """
        for tag, value in self._logon_tags.items():
            if value is None:
                continue
            message.setField(fix.StringField(int(tag), str(value)))

