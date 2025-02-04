// AutoCompleteInput.tsx
import { backendUrl } from "@/Envs";
import { Popover, PopoverContent, PopoverTrigger } from "@radix-ui/react-popover";
import React from "react";
import { useTranslation } from "react-i18next";
import { Card } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

// Update the interface to accept controlled props.
interface AutoCompleteInputProps {
    value: string;
    onChange: (value: string) => void;
    onBlur: () => void;
}

const AutoCompleteInput: React.FC<AutoCompleteInputProps> = ({ value, onChange, onBlur }) => {
    const [isPopoverOpen, setIsPopoverOpen] = React.useState(false);
    const [sellerNames, setSellerNames] = React.useState<string[]>([]);
    const [filteredSellerNames, setFilteredSellerNames] = React.useState<string[]>([]);
    const [activeIndex, setActiveIndex] = React.useState<number>(-1);

    React.useEffect(() => {
        fetchSellerNames().then(setSellerNames);
    }, []);

    const handleSetFocus = () => {
        setIsPopoverOpen(true);
    };

    async function fetchSellerNames(): Promise<string[]> {
        try {
            const response = await fetch(`${backendUrl}/meta/sellers`);
            if (!response.ok) {
                throw new Error(`Error: ${response.status} ${response.statusText}`);
            }
            return await response.json();
        } catch (error) {
            console.error("Failed to fetch seller names:", error);
            return [];
        }
    }

    const handleInputChange = (input: string) => {
        onChange(input); // update the form’s state
        if (!input) {
            setFilteredSellerNames([]);
            setActiveIndex(-1);
            return;
        }
        const filtered = sellerNames.filter((name) =>
            name.toLowerCase().includes(input.toLowerCase())
        );
        setFilteredSellerNames(filtered);
        setActiveIndex(-1);
    };

    // When the user selects a suggestion, update the form state
    const handleSelection = (selected: string) => {
        setIsPopoverOpen(false);
        onChange(selected);
        onBlur();
        console.log("Selected seller:", selected);
    };

    // Keyboard navigation for suggestions
    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (e.key === "ArrowDown") {
            e.preventDefault();
            if (filteredSellerNames.length > 0) {
                setActiveIndex((prevIndex) =>
                    prevIndex < filteredSellerNames.length - 1 ? prevIndex + 1 : 0
                );
            }
        } else if (e.key === "ArrowUp") {
            e.preventDefault();
            if (filteredSellerNames.length > 0) {
                setActiveIndex((prevIndex) =>
                    prevIndex <= 0 ? filteredSellerNames.length - 1 : prevIndex - 1
                );
            }
        } else if (e.key === "Enter" || e.key === "Tab") {
            if (activeIndex >= 0 && activeIndex < filteredSellerNames.length) {
                e.preventDefault();
                handleSelection(filteredSellerNames[activeIndex]);
                setActiveIndex(-1);
            }
        }
    };

    const { t } = useTranslation();

    return (
        <Popover open={isPopoverOpen}>
            <PopoverTrigger asChild>
                <Input
                    name="seller"
                    type="text"
                    onFocus={handleSetFocus}
                    placeholder="Lorem GmbH..."
                    value={value}
                    onBlur={() => handleSelection(value)}
                    onChange={(e) => handleInputChange(e.target.value)}
                    onKeyDown={handleKeyDown}
                />
            </PopoverTrigger>
            <PopoverContent
                onOpenAutoFocus={(e) => e.preventDefault()}
                className="w-full shadow-lg shadow-zinc-700 rounded-lg"
                style={{ width: 'var(--radix-popover-trigger-width)', zIndex: 999 }}
            >
                {filteredSellerNames.length > 0 && (
                    <Card className="w-full">
                        {filteredSellerNames.map((name, index) => (
                            <div
                                key={name}
                                className={`p-2 cursor-pointer w-full ${index === activeIndex
                                        ? "bg-gray-100 dark:bg-zinc-800" // highlighted style
                                        : "hover:bg-gray-100 dark:hover:bg-zinc-800"
                                    }`}
                                onMouseDown={(e) => e.preventDefault()}
                                onClick={() => handleSelection(name)}
                            >
                                {name}
                            </div>
                        ))}
                    </Card>
                )}
            </PopoverContent>
        </Popover>
    );
};

export default AutoCompleteInput;
