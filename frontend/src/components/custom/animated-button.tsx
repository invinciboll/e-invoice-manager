import { Button } from "@/components/ui/button";
import { Progress } from "@/types";
import React from "react";
import { useTranslation } from "react-i18next";

interface AnimatedButtonProps {
    translationIdentifier: string;
    onClick: () => void;
    disabled: boolean;
    progress: Progress;
    type?: "button" | "submit" | "reset";
}

const AnimatedButton: React.FC<AnimatedButtonProps> = ({
    translationIdentifier,
    onClick,
    disabled,
    progress,
    type,
}) => {
    const { t } = useTranslation();
    return (
        <Button
            onClick={onClick}
            variant="default"
            className={`text-black w-32 ${progress === "NOT_STARTED"
                    ? "bg-yellow-400 hover:bg-yellow-500"
                    : "bg-gray-500 hover:bg-gray-500"
                }`}
            disabled={disabled}
            type={type}
        >
            {progress === "IN_PROGRESS" ? (
                <svg
                    className="animate-spin h-5 w-5 text-white"
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                >
                    <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                    ></circle>
                    <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8v8H4z"
                    ></path>
                </svg>
            ) : progress === "DONE" ? (
                <span className="flex items-center">
                    <svg
                        className="w-5 h-5 text-white"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                        xmlns="http://www.w3.org/2000/svg"
                    >
                        <path
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            strokeWidth="4"
                            d="M5 13l4 4L19 7"
                        ></path>
                    </svg>
                </span>
            ) : (
                t(translationIdentifier)
            )}
        </Button>
    );
};

export default AnimatedButton;
