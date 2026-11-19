(() => {
    const phonePattern = /^(?:(?:\+?51)[ .-]?)?9(?:[ .-]?\d){8}$/;
    const imageTypes = new Set(["image/jpeg", "image/png", "image/webp"]);
    const imageExtensions = /\.(?:jpe?g|png|webp)$/i;
    const maxImageBytes = 15 * 1024 * 1024;

    function errorElement(field) {
        const key = field.name || field.id;
        if (!key) return null;
        const selector = `[data-client-error-for="${CSS.escape(key)}"]`;
        let error = field.parentElement?.querySelector(selector);
        if (!error) {
            error = document.createElement("span");
            error.dataset.clientErrorFor = key;
            error.id = `client-error-${key.replace(/[^a-zA-Z0-9_-]/g, "-")}`;
            error.className = "mt-1 block text-sm text-red-300";
            error.setAttribute("role", "alert");
            field.insertAdjacentElement("afterend", error);
        }
        const descriptions = new Set((field.getAttribute("aria-describedby") || "").split(/\s+/).filter(Boolean));
        descriptions.add(error.id);
        field.setAttribute("aria-describedby", [...descriptions].join(" "));
        return error;
    }

    function setFieldError(field, message) {
        const error = errorElement(field);
        field.setAttribute("aria-invalid", message ? "true" : "false");
        if (error) error.textContent = message || "";
        field.setCustomValidity(message || "");
    }

    function validateField(field, form) {
        if (!(field instanceof HTMLElement) || !("validity" in field) || field.disabled) return true;
        setFieldError(field, "");

        let message = "";
        const value = typeof field.value === "string" ? field.value.trim() : "";
        if (field.required && !value && field.type !== "checkbox" && field.type !== "file") {
            message = "Este campo es obligatorio.";
        } else if (field.type === "email" && value && !field.validity.valid) {
            message = "Ingresa un correo electrónico válido.";
        } else if (field.type === "tel" && value && !phonePattern.test(value)) {
            message = "Ingresa un celular peruano válido: 9 dígitos, empezando en 9 (o con prefijo +51).";
        } else if ((field.name === "documentoIdentidad" || field.id === "documentoIdentidad")
                && value && !/^\d{8}$/.test(value)) {
            message = "El DNI debe contener exactamente 8 dígitos.";
        } else if (field.type === "file" && field.files?.length) {
            const files = Array.from(field.files);
            const tooMany = field.multiple
                && files.length + form.querySelectorAll(".guardar-imagen-checkbox:checked").length > 10;
            if (tooMany) {
                message = "Puedes conservar y subir como máximo 10 imágenes.";
            } else if (files.some(file => !imageTypes.has(file.type)
                    && !(file.type === "" && imageExtensions.test(file.name)))) {
                message = "Usa imágenes JPG, PNG o WEBP.";
            } else if (files.some(file => file.size > maxImageBytes)) {
                message = "Cada imagen debe pesar 15 MB o menos.";
            }
        } else if (field.type === "number" && value) {
            const isPropertyFeature = ["habitaciones", "banos", "cocheras"].includes(field.name);
            const isArea = ["areaConstruida", "areaTotal"].includes(field.name);
            const propertyType = form.querySelector('[name="tipo"], #tipo')?.value;
            const minimum = isArea
                ? 0.01
                : isPropertyFeature && propertyType !== "TERRENO"
                    ? 1
                    : Number(field.min || 0);
            if (Number(value) < minimum) {
                message = isPropertyFeature && propertyType !== "TERRENO"
                    ? "Para este tipo de propiedad, el valor debe ser al menos 1."
                    : "El valor debe ser mayor que cero.";
            }
        }

        if (!message && !field.validity.valid) {
            message = field.validationMessage || "Revisa el valor ingresado.";
        }
        if (!message && ["confirmarPassword", "confirmar-password"].includes(field.name)) {
            const password = form.querySelector('[name="password"], [name="passwordNueva"]');
            if (password && field.value !== password.value) {
                message = "Las contraseñas no coinciden.";
            }
        }
        setFieldError(field, message);
        return !message;
    }

    function validateForm(form) {
        const fields = Array.from(form.elements).filter(field => field instanceof HTMLElement);
        const invalid = fields.filter(field => !validateField(field, form));
        if (invalid.length) invalid[0].focus();
        return invalid.length === 0;
    }

    function setBusy(form, busy) {
        form.querySelectorAll('button[type="submit"], input[type="submit"]').forEach(button => {
            button.disabled = busy;
            button.setAttribute("aria-busy", String(busy));
        });
    }

    async function readResponse(response) {
        const contentType = response.headers.get("content-type") || "";
        let body = null;
        if (response.status !== 204) {
            if (contentType.includes("application/json")) {
                body = await response.json();
            } else {
                const text = await response.text();
                body = text ? {mensaje: text.slice(0, 240)} : null;
            }
        }
        if (!response.ok) {
            const error = new Error(body?.mensaje || `La solicitud falló (${response.status}).`);
            error.status = response.status;
            error.fieldErrors = body?.errores || {};
            throw error;
        }
        return body;
    }

    function showErrors(form, error, banner) {
        let firstInvalid = null;
        Object.entries(error.fieldErrors || {}).forEach(([name, message]) => {
            const field = form?.elements.namedItem(name)
                || form?.querySelector(`#${CSS.escape(name)}`);
            if (field instanceof HTMLElement) {
                setFieldError(field, message);
                firstInvalid ||= field;
            } else if (banner) {
                banner.textContent = [banner.textContent, message].filter(Boolean).join(" ");
            }
        });
        if (firstInvalid) firstInvalid.focus();
        if (banner && !Object.keys(error.fieldErrors || {}).length) banner.textContent = error.message;
    }

    document.addEventListener("submit", event => {
        const form = event.target;
        if (!(form instanceof HTMLFormElement) || !validateForm(form)) {
            event.preventDefault();
            event.stopImmediatePropagation();
            return;
        }
        setBusy(form, true);
    }, true);

    document.addEventListener("invalid", event => {
        const field = event.target;
        if (field instanceof HTMLElement && field.form) validateField(field, field.form);
    }, true);

    document.addEventListener("input", event => {
        const field = event.target;
        if (field instanceof HTMLElement && field.form && field.getAttribute("aria-invalid") === "true") {
            validateField(field, field.form);
        }
    });

    document.addEventListener("change", event => {
        const field = event.target;
        if (field instanceof HTMLElement && field.form && field.getAttribute("aria-invalid") === "true") {
            validateField(field, field.form);
        }
    });

    window.TerraValidation = {validateForm, setBusy, readResponse, showErrors};
})();
