import Foundation
import UIKit

@objc(RCTInputCalculator)
class RCTInputCalculator: RCTBaseTextInputViewManager {
    override func view() -> UIView! {
        let textInputCalendar = TextInputCalculator(bridge: bridge)
        return textInputCalendar
    }

    override static func requiresMainQueueSetup() -> Bool {
        return true
    }
}

protocol CustomKeyboardDelegate: AnyObject {
    func keyDidPress(_ key: String)
    func clearText()
    func onBackSpace()
    func calculateResult()
}

class TextInputCalculator: RCTSinglelineTextInputView, CustomKeyboardDelegate {
    var bridge: RCTBridge?
    var textField: RCTUITextField! {
        return backedTextInputView as? RCTUITextField
    }

    var isInitialized = true

    @objc var value: String?

    func keyDidPress(_ key: String) {
        guard let textField = backedTextInputView as? UITextField else {
            return
        }
        textField.insertText(key)
        value! += key
        if let bridge = bridge {
            bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "\(key)", eventCount: 1)
        }
    }

    func clearText() {
        guard let textField = backedTextInputView as? UITextField else {
            return
        }
        value = ""
        textField.text = ""
        if let bridge = bridge {
            bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "clear", eventCount: 1)
        }
    }

    func onBackSpace() {
        value = value?.dropLast().description
        dispatchUI {
            if let view = CalculatorKeyboardModule.editingTextField,
               let range = view.selectedTextRange,
               let fromRange = view.position(from: range.start, offset: -1),
               let newRange = view.textRange(from: fromRange, to: range.start)
            {
                view.replace(newRange, withText: "")
            }
        }
        if let bridge = bridge {
            bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "back", eventCount: 1)
        }
    }

    func calculateResult() {
        guard let textField = backedTextInputView as? UITextField,
              let text = textField.text?.replacingOccurrences(of: "×", with: "*").replacingOccurrences(of: "÷", with: "/")
        else {
            return
        }

        let pattern = "^\\s*(-?\\d+(\\.\\d+)?\\s*[-+*/]\\s*)*-?\\d+(\\.\\d+)?\\s*$"
        let regex = try? NSRegularExpression(pattern: pattern)
        let range = NSRange(location: 0, length: text.utf16.count)

        if regex?.firstMatch(in: text, options: [], range: range) != nil {
            let expression = NSExpression(format: text)
            if let result = expression.expressionValue(with: nil, context: nil) as? NSNumber {
                textField.text = result.stringValue
                value = result.stringValue
                if let bridge = bridge {
                    bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "=", eventCount: 1)
                }
            }
        }
        else {
            print("Invalid expression")
        }
    }

    override init(bridge: RCTBridge) {
        super.init(bridge: bridge)
        self.bridge = bridge
        let inputView = CalculatorKeyboardView()
        inputView.frame = CGRect(x: 0, y: 0, width: UIScreen.main.bounds.size.width, height: UIScreen.main.bounds.size.width * 0.7)
        inputView.delegate = self

        if let textField = backedTextInputView as? RCTUITextField {
            textField.inputView = inputView
            textField.inputView?.reloadInputViews()
            textField.keyboardType = UIKeyboardType.default
        }
        textField?.textInputDelegate = self

//        if let value = value {
//            text = value
//            let attributedString = NSAttributedString(string: text, attributes: [NSAttributedString.Key.font: UIFont.systemFont(ofSize: 14), NSAttributedString.Key.foregroundColor: UIColor.black])
//            print("init \(value)")
//            textField?.attributedText = attributedString
//        }

//        textField.addTarget(self, action: #selector(textEditingChanged(_:)), for: .)
    }

//    override func reactSetFrame(_ frame: CGRect) {
//        super.reactSetFrame(frame)
//        textField?.frame = CGRect(x: 0, y: 0, width: frame.size.width, height: frame.size.height)
//    }

    override func textInputShouldBeginEditing() -> Bool {
        CalculatorKeyboardModule.editingTextField = backedTextInputView as? RCTUITextField
        return true
    }

//    override func didSetProps(_ changedProps: [String]!) {
//        super.didSetProps(changedProps)
//
//        if let value = value {
//            if let textField = backedTextInputView as? UITextField {
//                if textField.text == nil || textField.text == "" {
//                    textField.insertText(value)
//                    if let bridge = bridge {
//                        bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "init", eventCount: 1)
//                    }
//                }
//                else {
//                    textField.text = value
//                    if let bridge = bridge {
//                        bridge.eventDispatcher().sendTextEvent(with: .change, reactTag: reactTag, text: value, key: "init", eventCount: 1)
//                    }
//                }
//            }
//        }
//    }
}

class CalculatorKeyboardView: UIView {
    weak var delegate: CustomKeyboardDelegate?
    let keys: [[String]] = [
        ["AC", "÷", "×", "back"],
        ["7", "8", "9", "-"],
        ["4", "5", "6", "+"],
        ["1", "2", "3", "="],
        ["000", "", "0"]
    ]

    override init(frame: CGRect) {
        super.init(frame: frame)
    }

    @available(*, unavailable)
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func didMoveToWindow() {
        setupUI()
    }

    private func setupUI() {
        let SEPARATOR_WIDTH: CGFloat = 3

        let columns: CGFloat = 4
        let buttonWidth: CGFloat = ((frame.width > 0 ? frame.width : 375) - (columns - 1) * SEPARATOR_WIDTH - 3) / columns
        let buttonHeight = buttonWidth / 2

        // create a wrapper view
        let contentView = UIView(frame: CGRect(x: 0, y: 0, width: frame.width, height: frame.height))
        contentView.backgroundColor = UIColor(red: 249 / 255, green: 249 / 255, blue: 249 / 255, alpha: 1)
        contentView.directionalLayoutMargins = NSDirectionalEdgeInsets(top: 1.5,
                                                                       leading: 1.5,
                                                                       bottom: 20,
                                                                       trailing: 10)
        contentView.center = CGPoint(x: bounds.midX, y: bounds.midY)
        addSubview(contentView)

        // add button to wrapper view
        var yOffset: CGFloat = contentView.layoutMargins.top
        for (_, row) in keys.enumerated() {
            var xOffset: CGFloat = contentView.layoutMargins.left
            for (_, key) in row.enumerated() {
                let button = UIButton(type: .system)
                let specialKeys = ["=", "-", "×", "÷", "AC", "back", "+"]
                button.backgroundColor = UIColor.white
                button.layer.cornerRadius = 5
                button.setTitle(key, for: .normal)
                button.setTitleColor(.black, for: UIControl.State())
                button.titleLabel?.font = UIFont.systemFont(ofSize: 24, weight: .medium)
                button.frame = CGRect(x: xOffset, y: yOffset, width: buttonWidth, height: buttonHeight)
                button.nativeID = key

                if key == "" {
                    button.frame.size.height = CGFloat(0)
                }

                if key == "=" {
                    button.frame.size.height = buttonWidth + SEPARATOR_WIDTH
                }

                if key == "000" {
                    button.frame.size.width = buttonWidth * 2 + SEPARATOR_WIDTH
                }

                if key == "back" {
                    button.setTitle("", for: .normal)
                    button.setImage(UIImage(named: "back.png")?.withRenderingMode(.alwaysOriginal), for: .normal)
                }

                if specialKeys.contains(key) {
                    button.backgroundColor = UIColor(hex: "#d9d9d9")
                }

                button.addTarget(self, action: #selector(keyPressed(_:)), for: .touchUpInside)
                contentView.addSubview(button)
                xOffset += buttonWidth + SEPARATOR_WIDTH
            }
            yOffset += buttonHeight + SEPARATOR_WIDTH
        }
    }

    @objc private func keyPressed(_ sender: UIButton) {
        guard let key = sender.nativeID else { return }
        switch key {
        case "AC":
            delegate?.clearText()
        case "back":
            delegate?.onBackSpace()
        case "=":
            delegate?.calculateResult()
        case "+", "-", "÷", "×":
            delegate?.keyDidPress(" \(key) ")
        default:
            delegate?.keyDidPress(key)
        }
    }
}

public func dispatchUI(_ closure: @escaping () -> Void) {
    DispatchQueue.main.async { () in
        closure()
    }
}

@objc(CalculatorKeyboardModule)
class CalculatorKeyboardModule: NSObject, RCTBridgeModule {
    static var editingTextField: RCTUITextField?

    static func moduleName() -> String! {
        return "CalculatorKeyboardModule"
    }

    static func requiresMainQueueSetup() -> Bool {
        return true
    }

    @objc func insertText(_ text: String) {
        dispatchUI {
            if let view = CalculatorKeyboardModule.editingTextField,
               let range = view.selectedTextRange,
               let fromRange = view.position(from: range.start, offset: -1),
               var newRange = view.textRange(from: fromRange, to: range.start),
               view.compare(range.start, to: range.end).rawValue == 0,
               ["+", "-", "÷", "×"].contains(text)
            {
                if view.text(in: newRange) == " " {
                    if let fromRange = view.position(from: range.start, offset: -2) {
                        newRange = view.textRange(from: fromRange, to: range.start)!
                    }
                    let formatString = String(format: "%@ ", text)
                    view.replace(newRange, withText: formatString)
                }
                else {
                    view.replace(range, withText: text)
                }
            }
            else if let view = CalculatorKeyboardModule.editingTextField,
                    let range = view.selectedTextRange
            {
                view.replace(range, withText: text)
            }
        }
    }

    @objc func backSpace() {
        dispatchUI {
            if let view = CalculatorKeyboardModule.editingTextField,
               let range = view.selectedTextRange,
               let fromRange = view.position(from: range.start, offset: -1),
               var newRange = view.textRange(from: fromRange, to: range.start)
            {
                if view.compare(range.start, to: range.end).rawValue == 0 {
                    if view.text(in: newRange) == " " {
                        if let fromRange = view.position(from: range.start, offset: -2) {
                            newRange = view.textRange(from: fromRange, to: range.start)!
                        }
                    }
                    view.replace(newRange, withText: "")
                }
                else {
                    view.replace(range, withText: "")
                }
            }
        }
    }

    @objc func doDelete() {
        dispatchUI {
            if let view = CalculatorKeyboardModule.editingTextField {
                if let range = view.selectedTextRange {
                    if view.compare(range.start, to: range.end).rawValue == 0 {
                        if let toRange = view.position(from: range.start, offset: 1) {
                            if let newRange = view.textRange(from: range.start, to: toRange) {
                                view.replace(newRange, withText: "")
                            }
                        }
                        else {
                            if let allTextRange = view.textRange(from: view.beginningOfDocument, to: view.endOfDocument) {
                                view.replace(allTextRange, withText: "")
                            }
                        }
                    }
                    else {
                        view.replace(range, withText: "")
                    }
                }
            }
        }
    }
}
