document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById("adminMenuToggle");
    const backdrop = document.getElementById("adminSidebarBackdrop");
    const sidebar = document.getElementById("adminSidebar");
    if (!toggle || !backdrop || !sidebar) return;
    const mobileViewport = window.matchMedia("(max-width: 1023px)");

    const setOpen = open => {
        document.body.classList.toggle("sidebar-open", open);
        sidebar.inert = mobileViewport.matches && !open;
        toggle.setAttribute("aria-expanded", String(open));
        toggle.setAttribute("aria-label", open ? "Cerrar menú de navegación" : "Abrir menú de navegación");
        toggle.querySelector(".material-symbols-outlined").textContent = open ? "close" : "menu";
        backdrop.hidden = !open;
    };

    toggle.addEventListener("click", () => {
        setOpen(!document.body.classList.contains("sidebar-open"));
    });
    backdrop.addEventListener("click", () => setOpen(false));
    document.addEventListener("keydown", event => {
        if (event.key === "Escape" && document.body.classList.contains("sidebar-open")) {
            setOpen(false);
            toggle.focus();
        }
    });
    document.querySelectorAll(".sidebar-link").forEach(link => link.addEventListener("click", () => setOpen(false)));
    mobileViewport.addEventListener("change", () => setOpen(false));
    setOpen(false);
});

document.addEventListener("DOMContentLoaded", () => {
    const propertyDialog = document.getElementById("propertyEditorModal");
    const propertyContent = propertyDialog?.querySelector("[data-property-form-content]");
    let previewUrl;

    function initializePropertyEditor(container) {
        const form = container.querySelector("#propertyEditorForm");
        if (!form) return;

        const refreshCover = () => {
            const cards = [...container.querySelectorAll(".guardar-imagen-checkbox")]
                .map(checkbox => checkbox.closest("article"));
            const firstSaved = cards.find(card => card.querySelector(".guardar-imagen-checkbox").checked);
            cards.forEach(card => {
                const badge = card.querySelector("[data-cover-label]");
                badge?.classList.toggle("hidden", card !== firstSaved);
                card.classList.toggle("opacity-40", !card.querySelector(".guardar-imagen-checkbox").checked);
            });
        };

        container.querySelectorAll("[data-remove-image]").forEach(button => button.addEventListener("click", () => {
            const card = button.closest("article");
            const checkbox = card.querySelector(".guardar-imagen-checkbox");
            checkbox.checked = !checkbox.checked;
            button.textContent = checkbox.checked ? "X" : "+";
            button.setAttribute("aria-label", checkbox.checked ? "Quitar imagen" : "Conservar imagen");
            refreshCover();
        }));
        refreshCover();
        container.querySelector(".property-form-cancel")?.addEventListener("click", event => {
            if (!propertyDialog?.open) return;
            event.preventDefault();
            propertyDialog.close();
        });

        if (propertyDialog?.dataset.saveMode !== "ajax") return;
        form.addEventListener("submit", async event => {
            event.preventDefault();
            if (!form.reportValidity()) return;
            const button = form.querySelector('button[type="submit"]');
            button.disabled = true;
            try {
                const response = await fetch(form.action, {
                    method: "POST",
                    headers: {"Accept": "text/html"},
                    body: new FormData(form)
                });
                if (response.redirected && !new URL(response.url).pathname.startsWith("/login")) {
                    propertyDialog.close();
                    window.alert("Propiedad guardada correctamente.");
                    window.location.reload();
                    return;
                }
                const html = await response.text();
                const returnedPage = new DOMParser().parseFromString(html, "text/html");
                const returnedForm = returnedPage.querySelector("#property-form-panel");
                if (returnedForm) {
                    propertyContent.replaceChildren(returnedForm);
                    initializePropertyEditor(propertyContent);
                    const error = propertyContent.querySelector('[role="alert"]');
                    if (error && !error.textContent.trim()) {
                        error.textContent = "No se pudo guardar la propiedad. Revisa los datos e inténtalo de nuevo.";
                    }
                } else {
                    throw new Error(response.ok
                        ? "No se pudo validar la respuesta al guardar la propiedad."
                        : "No se pudo guardar la propiedad. Revisa los datos e inténtalo de nuevo.");
                }
            } catch (error) {
                let message = propertyContent.querySelector('[role="alert"]');
                if (!message) {
                    message = document.createElement("p");
                    message.setAttribute("role", "alert");
                    message.className = "text-sm text-red-300";
                    propertyContent.prepend(message);
                }
                message.textContent = error.message;
            } finally {
                const activeButton = propertyContent.querySelector('#propertyEditorForm button[type="submit"]');
                if (activeButton) activeButton.disabled = false;
            }
        });
    }

    async function openPropertyEditor(link) {
        if (!propertyDialog || !propertyContent) return;
        propertyContent.replaceChildren();
        const loading = document.createElement("p");
        loading.className = "p-6 text-sm text-on-surface-variant";
        loading.textContent = "Cargando formulario…";
        propertyContent.append(loading);
        propertyDialog.showModal();
        try {
            const response = await fetch(link.href, {headers: {"Accept": "text/html"}});
            if (!response.ok || response.redirected && new URL(response.url).pathname.startsWith("/login")) {
                throw new Error("No se pudo cargar el formulario de propiedad.");
            }
            const html = await response.text();
            const page = new DOMParser().parseFromString(html, "text/html");
            const panel = page.querySelector("#property-form-panel");
            if (!panel) throw new Error("La respuesta no contiene el formulario de propiedad esperado.");
            propertyContent.replaceChildren(panel);
            initializePropertyEditor(propertyContent);
        } catch (error) {
            const message = document.createElement("p");
            message.className = "p-6 text-sm text-red-300";
            message.setAttribute("role", "alert");
            message.textContent = error.message;
            propertyContent.replaceChildren(message);
        }
    }

    document.addEventListener("click", event => {
        const propertyLink = event.target.closest("[data-open-property-form]");
        if (propertyLink) {
            event.preventDefault();
            openPropertyEditor(propertyLink);
            return;
        }
        const closePropertyButton = event.target.closest("[data-close-property-editor]");
        if (closePropertyButton) propertyDialog?.close();

        const closeAgentButton = event.target.closest('[data-close-modal="agentModal"]');
        if (closeAgentButton) document.getElementById("agentModal")?.close();

        const photoTrigger = event.target.closest("#agentPhotoTrigger");
        if (photoTrigger) document.getElementById("agentPhotoInput")?.click();

        const statusButton = event.target.closest("[data-agent-status]");
        if (statusButton) {
            const form = statusButton.closest("form");
            form.querySelector("#agentStatusValue").value = statusButton.dataset.agentStatus;
            form.querySelectorAll("[data-agent-status]").forEach(option => {
                option.setAttribute("aria-pressed", String(option === statusButton));
            });
        }
    });

    document.addEventListener("change", event => {
        if (event.target.id !== "agentPhotoInput") return;
        const file = event.target.files[0];
        if (!file) return;
        if (previewUrl) URL.revokeObjectURL(previewUrl);
        previewUrl = URL.createObjectURL(file);
        const image = document.getElementById("agentAvatarPreview");
        image.src = previewUrl;
        image.hidden = false;
        document.getElementById("agentAvatarFallback").hidden = true;
    });

    document.getElementById("agentModal")?.addEventListener("close", () => {
        const form = document.getElementById("agentCreateForm");
        form?.reset();
        const image = document.getElementById("agentAvatarPreview");
        if (image) {
            image.hidden = true;
            image.removeAttribute("src");
        }
        const fallback = document.getElementById("agentAvatarFallback");
        if (fallback) fallback.hidden = false;
        if (previewUrl) {
            URL.revokeObjectURL(previewUrl);
            previewUrl = undefined;
        }
        form?.querySelectorAll("[data-agent-status]").forEach(option => {
            option.setAttribute("aria-pressed", String(option.dataset.agentStatus === "DISPONIBLE"));
        });
        const status = form?.querySelector("#agentStatusValue");
        if (status) status.value = "DISPONIBLE";
    });
});
