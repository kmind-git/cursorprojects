from __future__ import annotations

import datetime as _dt

import quickfix as fix


def build_test_request(test_req_id: str) -> fix.Message:
    msg = fix.Message()
    header = msg.getHeader()
    header.setField(fix.MsgType(fix.MsgType_TestRequest))
    msg.setField(fix.TestReqID(test_req_id))
    return msg


def send_test_request(session_id: fix.SessionID, test_req_id: str | None = None) -> None:
    if test_req_id is None:
        test_req_id = _dt.datetime.utcnow().strftime("TR-%Y%m%d-%H%M%S")
    msg = build_test_request(test_req_id)
    fix.Session.sendToTarget(msg, session_id)

