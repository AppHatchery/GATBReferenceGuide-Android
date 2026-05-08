// function to handle tab switching for any table
function activateTab(tableContainer, tabIndex) {
  if (!tableContainer) return;

  tableContainer
    .querySelectorAll(".tab-button, .tab, .tab, .tab")
    .forEach((tab) => {
      tab.classList.remove("active-option", "active-tab");
    });

  tableContainer
    .querySelectorAll(
      ".option-content, .tab-content, .tab-content, .tab-content"
    )
    .forEach((content) => {
      content.classList.remove("active-option", "active-tab");
    });

  // Add active class to selected tab and content
  const selectedTab = tableContainer.querySelectorAll(
    ".tab-button, .tab, .tab, .tab"
  )[tabIndex];
  const selectedContent = tableContainer.querySelectorAll(
    ".option-content, .tab-content, .tab-content, .tab-content"
  )[tabIndex];

  if (selectedTab) {
    if (selectedTab.classList.contains("tab")) {
      selectedTab.classList.add("active-tab");
    } else if (selectedTab.classList.contains("tab-button")) {
      selectedTab.classList.add("active-option");
    } else if (selectedTab.classList.contains("tab")) {
      selectedTab.classList.add("active-tab");
    } else if (selectedTab.classList.contains("tab")) {
      selectedTab.classList.add("active-tab");
    }
  }

  if (selectedContent) {
    if (selectedContent.classList.contains("tab-content")) {
      selectedContent.classList.add("active-tab");
    } else if (selectedContent.classList.contains("option-content")) {
      selectedContent.classList.add("active-option");
    } else if (selectedContent.classList.contains("tab-content")) {
      selectedContent.classList.add("active-tab");
    } else if (selectedContent.classList.contains("tab-content")) {
      selectedContent.classList.add("active-tab");
    }
  }
}

// Function to handle tab switching with event
function handleTabSwitch(event, tabIndex) {
  // Get the clicked button from the event
  const clickedButton = event.currentTarget;

  const tableContainer = clickedButton.closest(".uk-overflow-auto");
  if (!tableContainer) return;

  // Get all tab buttons in this container
  const tabButtons = tableContainer.querySelectorAll(
    ".tab-button, .tab, .tab, .tab"
  );

  // Find the index of the clicked button within its container
  const clickedIndex = Array.from(tabButtons).indexOf(clickedButton);

  // Generate a unique ID for the container if it doesn't have one
  if (!tableContainer.id) {
    tableContainer.id = "table-" + Math.random().toString(36).substr(2, 9);
  }

  // Switch to the correct tab
  activateTab(tableContainer, clickedIndex);
}

function switchTab(tabIndex, event) {
  handleTabSwitch(event, tabIndex);
}

// For Dropdown Togglers
function toggleItem(clickedTitle) {
  const itemContent = clickedTitle.nextElementSibling;

  itemContent.classList.toggle("active");

  const chevronUp = clickedTitle.querySelector(".chevron-up");
  chevronUp.classList.toggle("active");
}

document.addEventListener("DOMContentLoaded", function () {
  const tooltips = document.querySelectorAll(".info-icon");

  document.addEventListener("click", function (event) {
    if (!event.target.closest(".info-icon")) {
      document.querySelectorAll(".info-icon.active").forEach((activeIcon) => {
        activeIcon.classList.remove("active");
      });
    }
  });

  tooltips.forEach(function (icon) {
    let tooltip = icon.querySelector(".tooltip");
    if (!tooltip) {
      tooltip = document.createElement("div");
      tooltip.className = "tooltip";
      tooltip.textContent =
        icon.getAttribute("data-tooltip") || "Additional information";

      // Get positioning class from data attribute
      const positionClass =
        icon.getAttribute("data-tooltip-position") || "tooltip-center";
      tooltip.classList.add(positionClass);

      icon.appendChild(tooltip);
    }

    let isTooltipVisible = false;

    icon.addEventListener("click", function (event) {
      event.stopPropagation();

      document.querySelectorAll(".info-icon.active").forEach((activeIcon) => {
        if (activeIcon !== icon) {
          activeIcon.classList.remove("active");
          const otherTooltip = activeIcon.querySelector(".tooltip");
          if (otherTooltip) otherTooltip.style.display = "none";
        }
      });

      isTooltipVisible = !this.classList.contains("active");
      this.classList.toggle("active");
      tooltip.style.display = isTooltipVisible ? "block" : "none";
    });
  });
});
