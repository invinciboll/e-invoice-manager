
import {
    Popover,
    PopoverContent,
    PopoverTrigger,
} from "@/components/ui/popover";
import React from "react";
import { useTranslation } from "react-i18next";
import { Button } from "../ui/button";

interface AnimatedButtonProps {
    handleResetPage: () => void;
    triggerPopover: boolean;
}

export const ConfirmSavePopover: React.FC<AnimatedButtonProps> = ({ handleResetPage, triggerPopover }) => {
    const [showPopup, setShowPopup] = React.useState(false);
    const { t } = useTranslation();

    const handleButtonClick = () => {
        if (triggerPopover) {
            setShowPopup(true);
        } else {
            handleResetPage();
        }
    };

    const handleConfirmClose = () => {
        setShowPopup(false);
        handleResetPage();
    };

    const handleCancel = () => {
        setShowPopup(false);
    };

    return (
        <Popover open={showPopup} onOpenChange={setShowPopup}>
            <PopoverTrigger asChild>
                <Button
                    onClick={handleButtonClick}
                    variant="default"
                    className="text-black w-32 bg-yellow-400 hover:bg-yellow-500"
                    style={{ width: 'calc(2 * 8rem + 1rem)' }}
                >
                    {t("overview.button-close")}
                </Button>
            </PopoverTrigger>
            <PopoverContent className="w-72">
                <h2 className="text-lg font-semibold mb-2">
                    {t("popover.confirm-title")}
                </h2>
                <p className="text-gray-600 mb-4">
                    {t("popover.confirm-message")}
                </p>
                <div className="flex justify-end space-x-2">
                    <Button
                        onClick={handleCancel}
                        variant="outline"
                        className="text-gray-700"
                    >
                        {t("popover.cancel")}
                    </Button>
                    <Button
                        onClick={handleConfirmClose}
                        variant="default"
                        className="bg-red-500 text-white hover:bg-red-600"
                    >
                        {t("popover.confirm")}
                    </Button>
                </div>
            </PopoverContent>
        </Popover>);
}