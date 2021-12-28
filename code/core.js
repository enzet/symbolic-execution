function clear(elements) {
	[].forEach.call(elements, function (element) {
		// Restore defaults for element here:
		element.style.opacity = "0.2";
	});
}

elements = document.getElementsByClassName("element");
clear(elements);

[].forEach.call(elements, function (element) {
	element.addEventListener("mouseover", function (event) {
		clear(elements);
		[].forEach.call(element.getAttribute("class").split(" "), function (cl) {
			if (className != "element") {
				subElements = document.getElementsByClassName(className);
				[].forEach.call(subElements, function (subElement) {
					// Make element visible here:
					subElement.style.opacity = "1.0";
				});
			}
		});
	}, false);
});