from __future__ import annotations

import argparse
import configparser
import os
import signal
import sys
import time
from pathlib import Path
from typing import Dict

# Allow running directly from source tree without installation:
# - `python -m fixtest ...` (after `pip install -e .`) OR
# - `PYTHONPATH=src python -m fixtest ...` OR
# - `python src/fixtest/app.py ...`
if __package__ in (None, ""):
    sys.path.insert(0, str(Path(__file__).resolve().parents[1]))

import quickfix as fix  # noqa: E402

from fixtest.application import FixApplication  # noqa: E402


def _parse_logon_tags(cfg_path: Path) -> Dict[int, str]:
    """
    Extract LogonTag.<tag>=<value> from cfg ([DEFAULT] + [SESSION]).
    We parse it ourselves to avoid QuickFIX Python binding API variations.
    """
    parser = configparser.ConfigParser(interpolation=None)
    parser.optionxform = str  # preserve case
    with cfg_path.open("r", encoding="utf-8") as f:
        parser.read_file(f)

    tags: Dict[int, str] = {}
    # DEFAULT
    for k, v in parser.defaults().items():
        if k.startswith("LogonTag."):
            tag = int(k.split(".", 1)[1].strip())
            tags[tag] = v
    # SESSION (single)
    if parser.has_section("SESSION"):
        for k, v in parser.items("SESSION"):
            if k.startswith("LogonTag."):
                tag = int(k.split(".", 1)[1].strip())
                tags[tag] = v
    return tags


def _ensure_dirs(base_dir: Path) -> None:
    (base_dir / "var" / "store").mkdir(parents=True, exist_ok=True)
    (base_dir / "var" / "log").mkdir(parents=True, exist_ok=True)


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(prog="fixtest")
    ap.add_argument(
        "--config",
        default="config/fix_client.cfg",
        help="Path to QuickFIX session config (cfg).",
    )
    args = ap.parse_args(argv)

    base_dir = Path(__file__).resolve().parents[2]  # .../fixtest/
    cfg_path = (base_dir / args.config).resolve() if not os.path.isabs(args.config) else Path(args.config)
    if not cfg_path.exists():
        print(f"Config not found: {cfg_path}", file=sys.stderr)
        return 2

    _ensure_dirs(base_dir)

    settings = fix.SessionSettings(str(cfg_path))
    logon_tags = _parse_logon_tags(cfg_path)
    app = FixApplication(logon_tags=logon_tags)

    store_factory = fix.FileStoreFactory(settings)
    log_factory = fix.FileLogFactory(settings)
    initiator = fix.SocketInitiator(app, store_factory, settings, log_factory)

    stop_requested = False

    def _request_stop(*_args: object) -> None:
        nonlocal stop_requested
        stop_requested = True

    signal.signal(signal.SIGINT, _request_stop)
    try:
        signal.signal(signal.SIGTERM, _request_stop)
    except Exception:
        # Windows may not support SIGTERM in the same way.
        pass

    print(f"Starting FIX initiator with config: {cfg_path}")
    initiator.start()

    try:
        while not stop_requested:
            time.sleep(0.2)
    finally:
        print("Stopping FIX initiator...")
        initiator.stop()
        # Give QuickFIX time to flush logs/stores.
        time.sleep(0.5)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

