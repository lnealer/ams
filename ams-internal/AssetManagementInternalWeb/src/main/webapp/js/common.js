/*
 * Shared browser behaviour.
 *
 * Written against the DOM directly rather than against the vendored toolkit, so that the pieces
 * that matter - above all attaching the AJAX token to every request - do not depend on it.
 */
(function () {
    "use strict";

    /**
     * The per-session token the server issued, rendered into the page by the common header.
     * Every state-changing request has to carry it, or AjaxTokenInterceptor rejects the request.
     */
    function getAjaxToken() {
        var meta = document.querySelector('meta[name="ams-ajax-token"]');
        return meta ? meta.getAttribute("content") : null;
    }

    /**
     * Spring Security's CSRF token and the header name it expects it in.
     *
     * Separate from the AJAX token above: that one is checked by the Struts interceptor, this one
     * by Spring Security's filter, which runs first. A request missing this is refused with a bare
     * 403 before any application code sees it, so there is nothing to report back to the user - it
     * simply has to be sent.
     */
    function getCsrf() {
        var token = document.querySelector('meta[name="ams-csrf-token"]');
        var header = document.querySelector('meta[name="ams-csrf-header"]');
        if (!token || !header) {
            return null;
        }
        return { header: header.getAttribute("content"), token: token.getAttribute("content") };
    }

    /**
     * Posts form data to an application endpoint with both tokens attached.
     *
     * @param {string} url application-relative URL
     * @param {Object} data key/value pairs to send
     * @param {Function} onSuccess called with the parsed JSON body
     * @param {Function} onError called with a message
     */
    function post(url, data, onSuccess, onError) {
        var request = new XMLHttpRequest();
        var body = [];
        var key;
        var csrf;

        data = data || {};
        data.ajaxToken = getAjaxToken();

        for (key in data) {
            if (Object.prototype.hasOwnProperty.call(data, key) && data[key] !== null
                    && data[key] !== undefined) {
                body.push(encodeURIComponent(key) + "=" + encodeURIComponent(data[key]));
            }
        }

        request.open("POST", url, true);
        request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        csrf = getCsrf();
        if (csrf) {
            request.setRequestHeader(csrf.header, csrf.token);
        }
        request.onreadystatechange = function () {
            var parsed;
            if (request.readyState !== 4) {
                return;
            }
            try {
                parsed = JSON.parse(request.responseText);
            } catch (notJson) {
                if (onError) {
                    onError("The server returned an unexpected response.");
                }
                return;
            }
            // The server reports an expired session in the body, because the transport does not
            // reliably surface the status code to this handler.
            if (parsed && parsed.invalidSession) {
                window.location.reload();
                return;
            }
            if (request.status >= 200 && request.status < 300) {
                if (onSuccess) {
                    onSuccess(parsed);
                }
            } else if (onError) {
                onError((parsed && parsed.detail) || "The request could not be completed.");
            }
        };
        request.send(body.join("&"));
    }

    /**
     * Loads a dashboard panel's content.
     */
    function loadPanel(panel) {
        var url = panel.getAttribute("data-content-url");
        if (!url) {
            return;
        }
        post(url, {}, function () {
            panel.textContent = "";
        }, function (message) {
            panel.textContent = message;
        });
    }

    /** The six inputs that make up one contact, by the id prefix its fieldset uses. */
    var CONTACT_FIELDS = ["first", "last", "email", "phone", "ext", "mobile"];

    function contactInput(prefix, field) {
        return document.getElementById(prefix + "-" + field);
    }

    /**
     * Mirrors one contact onto another while a "same as" box is ticked.
     *
     * The server copies these on submit regardless - applyCopyBoxes in OrderContactAction is what
     * actually decides what is stored. This exists because without it the box appears to do
     * nothing: the operator ticks it, the fields below stay empty, and the only way to discover the
     * copy happened is to submit and come back. Mirroring on screen makes the box mean what it
     * says, and marking the target read only makes it obvious the values are not independently
     * editable until the box is cleared.
     */
    function bindSameAs(boxId, sourcePrefix, targetPrefix) {
        var box = document.getElementById(boxId);
        if (!box) {
            return null;
        }

        function apply() {
            var i, source, target;
            for (i = 0; i < CONTACT_FIELDS.length; i++) {
                source = contactInput(sourcePrefix, CONTACT_FIELDS[i]);
                target = contactInput(targetPrefix, CONTACT_FIELDS[i]);
                if (!source || !target) {
                    continue;
                }
                if (box.checked) {
                    target.value = source.value;
                    target.readOnly = true;
                    target.setAttribute("aria-readonly", "true");
                } else {
                    // The values stay behind on unticking: the operator almost always wants to
                    // amend the copy rather than retype it from nothing.
                    target.readOnly = false;
                    target.removeAttribute("aria-readonly");
                }
            }
        }

        box.addEventListener("change", function () {
            apply();
            // Installation may be copying from shipping, which has just changed underneath it.
            document.dispatchEvent(new CustomEvent("ams:contacts-changed"));
        });

        var j;
        for (j = 0; j < CONTACT_FIELDS.length; j++) {
            (function (input) {
                if (!input) {
                    return;
                }
                input.addEventListener("input", function () {
                    if (box.checked) {
                        apply();
                        document.dispatchEvent(new CustomEvent("ams:contacts-changed"));
                    }
                });
            }(contactInput(sourcePrefix, CONTACT_FIELDS[j])));
        }
        return apply;
    }

    /**
     * Wires both boxes on the ordering contact screen, and chains them: with both ticked, typing in
     * the ordering contact has to reach the installation contact through the shipping one.
     */
    function bindContactCopying() {
        var shipping = bindSameAs("ship-same", "oc", "sc");
        var installation = bindSameAs("install-same", "sc", "ic");
        if (!shipping && !installation) {
            return;
        }
        document.addEventListener("ams:contacts-changed", function () {
            if (installation) {
                installation();
            }
        });
        // Reflect the state the server rendered: coming back to this screen with the boxes already
        // ticked has to show the fields locked, not editable-looking.
        if (shipping) {
            shipping();
        }
        if (installation) {
            installation();
        }
    }

    function onReady() {
        var panels = document.querySelectorAll(".ams-panel-body[data-content-url]");
        var i;
        for (i = 0; i < panels.length; i++) {
            loadPanel(panels[i]);
        }
        bindContactCopying();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", onReady);
    } else {
        onReady();
    }

    window.ams = { post: post, getAjaxToken: getAjaxToken, getCsrf: getCsrf };
}());
