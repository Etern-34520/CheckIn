const mobile = ref(false);
const colorScheme = ref("light");
const htmlHtmlElement = document.querySelector("html");
const checkColorScheme = () => {
    if (window.matchMedia('(prefers-color-scheme: dark)').matches) {
        if (htmlHtmlElement.classList.contains("light")) {
            htmlHtmlElement.classList.remove("light");
        }
        htmlHtmlElement.classList.add("dark");
        colorScheme.value = "dark";
    } else {
        if (htmlHtmlElement.classList.contains("dark")) {
            htmlHtmlElement.classList.remove("dark");
        }
        htmlHtmlElement.classList.add("light");
        // htmlHtmlElement.className = "light";
        colorScheme.value = "light";
    }
};
const checkMobile = () => {
    mobile.value = window.innerWidth <= 720;
    if (mobile.value) {
        if (!htmlHtmlElement.classList.contains("mobile")) {
            htmlHtmlElement.classList.add("mobile");
        }
    } else {
        htmlHtmlElement.classList.remove("mobile");
    }
};
checkColorScheme();
checkMobile();
window.addEventListener("resize",checkMobile);
window.matchMedia('(prefers-color-scheme: dark)').addEventListener("change",checkColorScheme);
export default {
    mobile:mobile,
    colorScheme: colorScheme,
};