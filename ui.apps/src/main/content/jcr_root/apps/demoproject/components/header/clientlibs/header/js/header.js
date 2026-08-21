(function () {
    "use strict";

    console.log("demoproject.header JS loaded");

    document.addEventListener("DOMContentLoaded", function () {

        var navItems = document.querySelectorAll(".site-header .nav-item");

        navItems.forEach(function (navItem) {

            var megaMenu = navItem.querySelector(":scope > .mega-menu");

            if (!megaMenu) {
                return;
            }

            var navLink = navItem.querySelector(":scope > .nav-link");

            if (!navLink) {
                return;
            }

            navLink.addEventListener("click", function (event) {

                event.preventDefault();

                var isOpen = navItem.classList.contains("is-open");

                /* Close all other menus */

                navItems.forEach(function (item) {
                    item.classList.remove("is-open");
                });

                /* Open the clicked menu */

                if (!isOpen) {
                    navItem.classList.add("is-open");
                }

            });

        });


        /* Close menu when clicking outside the header */

        document.addEventListener("click", function (event) {

            if (!event.target.closest(".site-header")) {

                navItems.forEach(function (navItem) {
                    navItem.classList.remove("is-open");
                });

            }

        });


        /* Close menu with Escape */

        document.addEventListener("keydown", function (event) {

            if (event.key === "Escape") {

                navItems.forEach(function (navItem) {
                    navItem.classList.remove("is-open");
                });

            }

        });

    });

})();