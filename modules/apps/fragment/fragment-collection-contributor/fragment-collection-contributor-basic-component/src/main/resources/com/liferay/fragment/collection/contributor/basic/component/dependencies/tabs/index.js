const dropdown = fragmentElement.querySelector('.component-tabs__collapse');
const dropdownButton = fragmentElement.querySelector(
	'.component-tabs__navbar-toggler'
);
const editMode = layoutMode === 'edit';
const persistedTabKey = 'tabsFragment_' + fragmentNamespace + '_persistedTabId';

const tabItems = [].slice.call(
	fragmentElement.querySelectorAll(
		'[data-fragment-namespace="' +
			fragmentNamespace +
			'"].component-tabs__nav-link'
	)
);

const tabPanelItems = [].slice.call(
	fragmentElement.querySelectorAll(
		'[data-fragment-namespace="' +
			fragmentNamespace +
			'"].component-tabs__tab-panel-item'
	)
);

const persistedTab = (function () {
	if (!configuration.persistSelectedTab) {
		let persistedId;

		return {
			getId() {
				return persistedId;
			},

			setId(nextId) {
				persistedId = nextId;
			},
		};
	}

	return {
		getId() {
			return Number(sessionStorage.getItem(persistedTabKey));
		},

		setId(id) {
			sessionStorage.setItem(persistedTabKey, id);
		},
	};
})();

function activeTab(item) {
	tabItems.forEach(function (tabItem) {
		tabItem.setAttribute('aria-selected', false);
		tabItem.classList.remove('active');
	});

	item.setAttribute('aria-selected', true);
	item.classList.add('active');
}

function activeTabPanel(item) {
	tabPanelItems.forEach(function (tabPanelItem) {
		if (tabPanelItem.style.display !== 'none') {
			tabPanelItem.style.display = 'none';
		}
	});

	item.style.display = '';
}

function handleDropdown(event, item) {
	event.preventDefault();
	dropdown.classList.toggle('show');

	const ariaExpanded = dropdownButton.getAttribute('aria-expanded');

	dropdownButton.setAttribute(
		'aria-expanded',
		ariaExpanded === 'false' ? true : false
	);

	if (item) {
		handleDropdownButtonName(item);
	}
}

function handleDropdownButtonName(item) {
	const tabText =
		item.querySelector('lfr-editable') ||
		item.querySelector('.component-tabs__navbar-text');

	if (tabText) {
		dropdownButton.querySelector('.component-tabs__navbar-text').innerHTML =
			tabText.textContent;
	}
}

function openTabPanel(event, i) {
	const currentTarget = event.currentTarget;
	const target = event.target;

	const isEditable =
		target.hasAttribute('data-lfr-editable-id') ||
		target.hasAttribute('contenteditable');

	const dropdownIsOpen = JSON.parse(
		dropdownButton.getAttribute('aria-expanded')
	);

	if (!isEditable || !editMode) {
		if (dropdownIsOpen) {
			handleDropdown(event, currentTarget);
		}
		else {
			handleDropdownButtonName(currentTarget);
		}

		currentTarget.focus();

		activeTab(currentTarget, i);
		activeTabPanel(tabPanelItems[i]);
		persistedTab.setId(i);
	}
}

function main() {
	const tabItemId = tabItems[persistedTab.getId()] ? persistedTab.getId() : 0;

	tabItems.forEach(function (item, index) {
		item.addEventListener('click', function (event) {
			openTabPanel(event, index);
		});
	});

	dropdownButton.addEventListener('click', function (event) {
		handleDropdown(event);
	});

	activeTab(tabItems[tabItemId]);
	activeTabPanel(tabPanelItems[tabItemId]);
	handleDropdownButtonName(tabItems[tabItemId]);
}

main();
