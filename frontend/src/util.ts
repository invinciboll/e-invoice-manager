import i18n from "i18next";

export function formatDate(dateString: string): string {
    const locale = i18n.language; // Get the current language (e.g., 'en', 'de')
    const date = new Date(dateString);
    return new Intl.DateTimeFormat(locale, {
        year: "numeric",
        month: "numeric",
        day: "numeric",
    }).format(date);
};